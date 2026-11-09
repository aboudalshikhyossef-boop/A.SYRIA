#!/bin/sh
# Wait for Android's Package Manager before installing the app under test.
# Newer emulator images can report sys.boot_completed before package is registered.
set -eu

emulator_serial="${ANDROID_SERIAL:-emulator-5554}"

adb devices -l
adb -s "$emulator_serial" wait-for-device

is_package_manager_ready() {
  boot_completed="$(adb -s "$emulator_serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  package_probe="$(adb -s "$emulator_serial" shell cmd package list packages 2>&1 || true)"

  [ "$boot_completed" = "1" ] || return 1

  case "$package_probe" in
    package:*) return 0 ;;
    *) return 1 ;;
  esac
}

wait_for_package_manager() {
  attempt=0
  while [ "$attempt" -lt 90 ]; do
    attempt=$((attempt + 1))
    if is_package_manager_ready; then
      echo "Android boot and Package Manager are ready."
      return 0
    fi

    echo "Waiting for Package Manager (attempt $attempt/90)."
    sleep 2
  done

  return 1
}

if ! wait_for_package_manager; then
  echo "::warning::Package Manager did not become ready; restarting the emulator once."
  adb -s "$emulator_serial" reboot
  adb -s "$emulator_serial" wait-for-device

  if ! wait_for_package_manager; then
    echo "::error::Android Package Manager remained unavailable after an emulator restart."
    adb -s "$emulator_serial" shell getprop init.svc.system_server || true
    adb -s "$emulator_serial" shell service check package || true

    diagnostic_log="$(adb -s "$emulator_serial" logcat -d -b all -v time -t 2000 2>&1 || true)"
    printf '%s\n' "$diagnostic_log" |
      grep -Ei 'FATAL EXCEPTION|SystemServer|PackageManager|ActivityManager|OutOfMemory|watchdog|Fatal signal|lmkd' |
      tail -n 200 || true
    exit 1
  fi
fi

api_level="$(adb -s "$emulator_serial" shell getprop ro.build.version.sdk)"
if [ "$api_level" -ge 37 ]; then
  # Android 17's emulator image can deny GET_USAGE_STATS to the "android"
  # package while PackageInstaller queries StorageStatsService. Set the
  # package-level AppOp because that service checks package "android" (UID 1000).
  # This affects only the disposable CI emulator, never the application.
  echo "Allowing GET_USAGE_STATS for the Android system package on API $api_level."
  adb -s "$emulator_serial" shell cmd appops set android GET_USAGE_STATS allow

  appops_state="$(adb -s "$emulator_serial" shell cmd appops get android GET_USAGE_STATS)"
  printf '%s\n' "$appops_state"
  case "$appops_state" in
    *"GET_USAGE_STATS: allow"*) ;;
    *)
      echo "::error::Android system package GET_USAGE_STATS AppOp is not allowed."
      exit 1
      ;;
  esac
fi
\n
# The caller can pass the Gradle instrumentation command. Running it here lets
# us capture emulator diagnostics before android-emulator-runner shuts the AVD down.
echo "Android test device diagnostics:"
adb -s "$emulator_serial" shell getprop ro.build.version.sdk || true
adb -s "$emulator_serial" shell getprop init.svc.system_server || true
adb -s "$emulator_serial" shell service check package || true
adb -s "$emulator_serial" shell df -h /data || true

if [ "$#" -gt 0 ]; then
  set +e
  "$@"
  test_exit_code=$?
  set -e

  if [ "$test_exit_code" -ne 0 ]; then
    echo "::error::Android instrumentation command failed with exit code $test_exit_code."
    echo "Collecting Package Manager, system_server, storage, and low-memory diagnostics."
    adb -s "$emulator_serial" shell getprop init.svc.system_server || true
    adb -s "$emulator_serial" shell service check package || true
    adb -s "$emulator_serial" shell df -h /data || true
    if [ "$api_level" -ge 37 ]; then
      adb -s "$emulator_serial" shell cmd appops get android GET_USAGE_STATS || true
    fi

    diagnostic_log="$(adb -s "$emulator_serial" logcat -d -b all -v threadtime -t 5000 2>&1 || true)"
    printf '%s\n' "$diagnostic_log" |
      grep -Ei 'FATAL EXCEPTION|SystemServer|system_server|PackageManager|PackageInstaller|StorageStats|ActivityManager|OutOfMemory|lmkd|watchdog|Fatal signal|installd|low memory|Broken pipe' |
      tail -n 300 || true
  fi

  exit "$test_exit_code"
fi
