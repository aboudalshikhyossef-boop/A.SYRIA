package com.example.worker

  import android.app.NotificationChannel
  import android.app.NotificationManager
  import android.content.Context
  import android.content.pm.PackageManager
  import android.os.Build
  import android.provider.Settings
  import androidx.core.app.NotificationCompat
  import androidx.work.CoroutineWorker
  import androidx.work.WorkerParameters
  import com.batoulapps.adhan.CalculationMethod
  import com.batoulapps.adhan.Coordinates
  import com.batoulapps.adhan.Madhab
  import com.batoulapps.adhan.Prayer
  import com.batoulapps.adhan.PrayerTimes
  import com.batoulapps.adhan.data.DateComponents
  import com.example.data.SovereignDataStore
  import com.google.ai.client.generativeai.GenerativeModel
  import kotlinx.coroutines.flow.first
  import java.util.Calendar
  import java.util.Date

  class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

      companion object {
          const val CHANNEL_PRAYER = "prayer_alerts"
          const val CHANNEL_SECURITY = "security_alerts"
          const val CHANNEL_WISDOM = "daily_wisdom"

          private const val PREFS_THROTTLE = "sovereign_notif_throttle"
          private const val KEY_LAST_SECURITY = "last_security_ms"
          private const val KEY_LAST_GEMINI = "last_gemini_ms"
          private const val KEY_LAST_WISDOM = "last_wisdom_ms"
          private const val TWELVE_HOURS_MS = 24L * 60 * 60 * 1000 // Reduced frequency to avoid Play Protect flags
          private const val TWENTY_FOUR_HOURS_MS = 24L * 60 * 60 * 1000

          private val SHORT_DUAS = mapOf(
              Prayer.FAJR to "اللّهمّ إنّي أسألك علماً نافعاً ورزقاً طيّباً وعملاً متقبّلاً",
              Prayer.SUNRISE to "اللّهمّ اجعل في قلبي نوراً وفي بصري نوراً وفي سمعي نوراً",
              Prayer.DHUHR to "ربّ اشرح لي صدري ويسّر لي أمري واحلل عقدةً من لساني",
              Prayer.ASR to "اللّهمّ إنّي أعوذ بك من علمٍ لا ينفع ومن قلبٍ لا يخشع",
              Prayer.MAGHRIB to "اللّهمّ إنّي أسألك العافية في الدنيا والآخرة",
              Prayer.ISHA to "اللّهمّ قني عذابك يوم تبعث عبادك"
          )

          private val PRAYER_MOTIVATIONS = mapOf(
              Prayer.FAJR to "من صلّى الفجر في جماعة فهو في ذمّة الله حتّى يُمسي. أقبِل على ربّك وقلبك مطمئن",
              Prayer.SUNRISE to "أشرقت الشمس بنور ربّها، فاذكر الله واشكره على نعمة يومٍ جديد",
              Prayer.DHUHR to "في وسط انشغال يومك، توقّف وقف بين يدي الله. الصلاة خير من كلّ ما يشغلك",
              Prayer.ASR to "هي الصلاة الوسطى التي أوصانا الله بالمحافظة عليها. لا تُفرّط فيها أبداً",
              Prayer.MAGHRIB to "غربت شمس يومك، فاشكر الله على ما مضى واستغفره لما فات",
              Prayer.ISHA to "اختم يومك بصلاةٍ تُنير قلبك وتُريح بالك. من صلّى العشاء في جماعة فكأنّما قام نصف الليل"
          )

          private val DAILY_WISDOMS = listOf(
              "﴿إِنَّ مَعَ الْعُسْرِ يُسْرًا﴾ — الشرح: ٦",
              "﴿وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ﴾ — الطلاق: ٣",
              "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾ — البقرة: ١٥٢",
              "﴿وَقُل رَّبِّ زِدْنِي عِلْمًا﴾ — طه: ١١٤",
              "﴿رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ﴾ — البقرة: ٢٠١",
              "﴿وَلَا تَيْأَسُوا مِن رَّوْحِ اللَّهِ إِنَّهُ لَا يَيْأَسُ مِن رَّوْحِ اللَّهِ إِلَّا الْقَوْمُ الْكَافِرُونَ﴾ — يوسف: ٨٧",
              "﴿ادْعُونِي أَسْتَجِبْ لَكُمْ﴾ — غافر: ٦٠"
          )
      }

      override suspend fun doWork(): Result {
          val dataStore = SovereignDataStore(applicationContext)
          val isAr = dataStore.isArabic.first()
          val apiKey = dataStore.geminiApiKey.first().ifBlank {
              com.asyria.v4.BuildConfig.GEMINI_API_KEY
          }
          val throttlePrefs = applicationContext.getSharedPreferences(PREFS_THROTTLE, Context.MODE_PRIVATE)
          val nowMs = System.currentTimeMillis()

          createNotificationChannels()

          // 1. Prayer alerts — always check every 15 min (has its own per-prayer deduplication)
          checkAndNotifyPrayerPrecise(isAr)

          // 2. Device Security Scan — throttled to once every 12 hours
          val lastSecurity = throttlePrefs.getLong(KEY_LAST_SECURITY, 0L)
          if (nowMs - lastSecurity >= TWELVE_HOURS_MS) {
              performDeviceSecurityScan(isAr)
              throttlePrefs.edit().putLong(KEY_LAST_SECURITY, nowMs).apply()
          }

          // 3. Gemini Security Tip — throttled to once every 12 hours
          val lastGemini = throttlePrefs.getLong(KEY_LAST_GEMINI, 0L)
          if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && nowMs - lastGemini >= TWELVE_HOURS_MS) {
              try {
                  val model = GenerativeModel(modelName = "gemini-2.5-flash-lite", apiKey = apiKey)
                  val promptLocale = if (isAr) "Arabic" else "English"
                  val prompt = "Provide one very brief (12 words max) high-priority cybersecurity tip in $promptLocale. Be direct. No intro."
                  val res = model.generateContent(prompt).text ?: ""
                  if (res.isNotBlank()) {
                      showNotification(
                          title = if (isAr) "تنبيه أمني سيادي" else "Sovereign Intelligence Alert",
                          message = res.trim(),
                          id = 1002,
                          channelId = CHANNEL_SECURITY
                      )
                      throttlePrefs.edit().putLong(KEY_LAST_GEMINI, nowMs).apply()
                  }
              } catch (e: Exception) {
                  e.printStackTrace()
              }
          }

          // 4. Daily Quranic Wisdom — throttled to once every 24 hours
          val lastWisdom = throttlePrefs.getLong(KEY_LAST_WISDOM, 0L)
          if (nowMs - lastWisdom >= TWENTY_FOUR_HOURS_MS) {
              sendDailyWisdom(isAr)
              throttlePrefs.edit().putLong(KEY_LAST_WISDOM, nowMs).apply()
          }

          return Result.success()
      }

      private fun sendDailyWisdom(isAr: Boolean) {
          val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
          val wisdom = DAILY_WISDOMS[dayOfYear % DAILY_WISDOMS.size]
          showNotification(
              title = if (isAr) "حكمة اليوم من القرآن الكريم" else "Daily Quranic Wisdom",
              message = wisdom,
              id = 5000 + dayOfYear,
              channelId = CHANNEL_WISDOM
          )
      }

      private fun createNotificationChannels() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

              val prayerChannel = NotificationChannel(
                  CHANNEL_PRAYER,
                  "تنبيهات الصلاة",
                  NotificationManager.IMPORTANCE_HIGH
              ).apply {
                  description = "تنبيهات مواقيت الصلاة والأذان"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                  enableLights(true)
                  lightColor = android.graphics.Color.GREEN
                  val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                  setSound(soundUri, android.media.AudioAttributes.Builder()
                      .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                      .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                      .build())
              }

              val securityChannel = NotificationChannel(
                  CHANNEL_SECURITY,
                  "تنبيهات الأمان",
                  NotificationManager.IMPORTANCE_HIGH
              ).apply {
                  description = "تنبيهات فحص أمان الجهاز والتطبيقات"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 500)
                  enableLights(true)
                  lightColor = android.graphics.Color.RED
                  val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                  setSound(soundUri, android.media.AudioAttributes.Builder()
                      .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                      .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                      .build())
              }

              val wisdomChannel = NotificationChannel(
                  CHANNEL_WISDOM,
                  "حكمة يومية",
                  NotificationManager.IMPORTANCE_DEFAULT
              ).apply {
                  description = "حكمة يومية من القرآن الكريم"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 200, 100, 200)
                  enableLights(true)
                  lightColor = android.graphics.Color.YELLOW
              }

              notificationManager.createNotificationChannel(prayerChannel)
              notificationManager.createNotificationChannel(securityChannel)
              notificationManager.createNotificationChannel(wisdomChannel)
          }
      }

      private fun checkAndNotifyPrayerPrecise(isAr: Boolean) {
          val prefs = applicationContext.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
          val lat = prefs.getFloat("last_lat", 33.5138f).toDouble()
          val lon = prefs.getFloat("last_lon", 36.2765f).toDouble()

          val coords = Coordinates(lat, lon)
          val date = DateComponents.from(Date())
          val params = CalculationMethod.UMM_AL_QURA.parameters.also { it.madhab = Madhab.SHAFI }
          val prayerTimes = PrayerTimes(coords, date, params)

          val now = Calendar.getInstance()
          val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

          val prayers = listOf(
              Prayer.FAJR to prayerTimes.fajr,
              Prayer.SUNRISE to prayerTimes.sunrise,
              Prayer.DHUHR to prayerTimes.dhuhr,
              Prayer.ASR to prayerTimes.asr,
              Prayer.MAGHRIB to prayerTimes.maghrib,
              Prayer.ISHA to prayerTimes.isha
          )

          val arabicNames = mapOf(
              Prayer.FAJR to "الفجر",
              Prayer.SUNRISE to "الشروق",
              Prayer.DHUHR to "الظهر",
              Prayer.ASR to "العصر",
              Prayer.MAGHRIB to "المغرب",
              Prayer.ISHA to "العشاء"
          )

          for ((prayer, prayerDate) in prayers) {
              if (prayerDate == null) continue
              val prayerCal = Calendar.getInstance().apply { time = prayerDate }
              val prayerMinutes = prayerCal.get(Calendar.HOUR_OF_DAY) * 60 + prayerCal.get(Calendar.MINUTE)

              val minutesBefore = prayerMinutes - currentMinutes
              val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
              val timeStr = timeFormat.format(prayerCal.time)

              val lastNotified5Min = prefs.getString("last_5min_${prayer.name}", "")
              val lastNotifiedExact = prefs.getString("last_exact_${prayer.name}", "")
              val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date())

              if (minutesBefore in 1..15 && lastNotified5Min != todayDate) {
                  val name = if (isAr) (arabicNames[prayer] ?: prayer.name) else prayer.name
                  val dua = SHORT_DUAS[prayer] ?: ""
                  showNotification(
                      title = if (isAr) "🤲 استعد لصلاة $name — $timeStr"
                              else "🤲 Prepare for ${prayer.name} — $timeStr",
                      message = if (isAr)
                          "بقي ٥ دقائق على أذان $name. توضّأ واستعد للقاء ربّك.\n\n$dua"
                      else
                          "5 minutes until ${prayer.name} at $timeStr. Make wudu and prepare.\n\n$dua",
                      id = 2000 + prayer.ordinal(),
                      channelId = CHANNEL_PRAYER
                  )
                  prefs.edit().putString("last_5min_${prayer.name}", todayDate).apply()
              }

              if (minutesBefore in -10..2 && lastNotifiedExact != todayDate) {
                  val name = if (isAr) (arabicNames[prayer] ?: prayer.name) else prayer.name
                  val motivation = PRAYER_MOTIVATIONS[prayer] ?: ""
                  showNotification(
                      title = if (isAr) "🕌 الله أكبر — حان وقت صلاة $name"
                              else "🕌 Allahu Akbar — Time for ${prayer.name}",
                      message = if (isAr)
                          "حيّ على الصلاة، حيّ على الفلاح.\n\n$motivation"
                      else
                          "Come to prayer, come to success.\n\n$motivation",
                      id = 3000 + prayer.ordinal(),
                      channelId = CHANNEL_PRAYER
                  )
                  prefs.edit().putString("last_exact_${prayer.name}", todayDate).apply()
              }
          }
      }

      private fun performDeviceSecurityScan(isAr: Boolean) {
          val pm = applicationContext.packageManager
          val dangerousPermissions = mapOf(
              android.Manifest.permission.CAMERA to (if (isAr) "الكاميرا" else "Camera"),
              android.Manifest.permission.RECORD_AUDIO to (if (isAr) "الميكروفون" else "Microphone"),
              android.Manifest.permission.READ_CONTACTS to (if (isAr) "جهات الاتصال" else "Contacts"),
              android.Manifest.permission.READ_SMS to (if (isAr) "الرسائل النصية" else "SMS")
          )

          var securityNotificationId = 4000

          val installedApps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
          for (packageInfo in installedApps) {
              val appInfo = packageInfo.applicationInfo ?: continue
              if (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) continue
              if (packageInfo.packageName == applicationContext.packageName) continue

              val requestedPermissions = packageInfo.requestedPermissions ?: continue
              val grantedDangerous = mutableListOf<String>()

              for ((permission, label) in dangerousPermissions) {
                  if (requestedPermissions.contains(permission)) {
                      grantedDangerous.add(label)
                  }
              }

              if (grantedDangerous.size >= 2) {
                  val appName = pm.getApplicationLabel(appInfo).toString()
                  val permList = grantedDangerous.joinToString(" و")
                  showNotification(
                      title = if (isAr) "تحذير أمني: صلاحيات خطيرة" else "Security Warning: Dangerous Permissions",
                      message = if (isAr)
                          "تحذير: تطبيق $appName لديه صلاحية الوصول إلى $permList. راجع صلاحيات هذا التطبيق وتأكّد من أنّك تثق به."
                      else
                          "Warning: $appName has access to ${grantedDangerous.joinToString(" and ")}. Review this app's permissions.",
                      id = securityNotificationId++,
                      channelId = CHANNEL_SECURITY
                  )
              }
          }

          val devOptionsEnabled = try {
              Settings.Secure.getInt(
                  applicationContext.contentResolver,
                  Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
              ) != 0
          } catch (_: Exception) { false }

          if (devOptionsEnabled) {
              showNotification(
                  title = if (isAr) "تنبيه أمني: خيارات المطوّر مفعّلة" else "Security Alert: Developer Options Enabled",
                  message = if (isAr)
                      "خيارات المطوّر مفعّلة على جهازك. هذا قد يُعرّض جهازك لمخاطر أمنية. يُنصح بتعطيلها إن لم تكن بحاجة إليها."
                  else
                      "Developer options are enabled on your device. This may expose your device to security risks. Disable if not needed.",
                  id = securityNotificationId++,
                  channelId = CHANNEL_SECURITY
              )
          }

          val usbDebuggingEnabled = try {
              Settings.Secure.getInt(
                  applicationContext.contentResolver,
                  Settings.Global.ADB_ENABLED, 0
              ) != 0
          } catch (_: Exception) { false }

          if (usbDebuggingEnabled) {
              showNotification(
                  title = if (isAr) "تحذير: تصحيح USB مفعّل" else "Warning: USB Debugging Enabled",
                  message = if (isAr)
                      "تصحيح أخطاء USB مفعّل. هذا يسمح بالوصول إلى بيانات جهازك عبر الحاسوب. قم بتعطيله لحماية خصوصيتك."
                  else
                      "USB debugging is enabled. This allows access to your device data via computer. Disable it to protect your privacy.",
                  id = securityNotificationId++,
                  channelId = CHANNEL_SECURITY
              )
          }

          val unknownSourcesEnabled = try {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  applicationContext.packageManager.canRequestPackageInstalls()
              } else {
                  @Suppress("DEPRECATION")
                  Settings.Secure.getInt(
                      applicationContext.contentResolver,
                      Settings.Secure.INSTALL_NON_MARKET_APPS, 0
                  ) != 0
              }
          } catch (_: Exception) { false }

          if (unknownSourcesEnabled) {
              showNotification(
                  title = if (isAr) "تحذير: مصادر غير معروفة مفعّلة" else "Warning: Unknown Sources Enabled",
                  message = if (isAr)
                      "تثبيت التطبيقات من مصادر غير معروفة مُفعّل. هذا يزيد خطر تثبيت تطبيقات ضارة. قم بتعطيله من الإعدادات."
                  else
                      "Installing apps from unknown sources is enabled. This increases the risk of malware. Disable it in settings.",
                  id = securityNotificationId++,
                  channelId = CHANNEL_SECURITY
              )
          }
      }

      private fun Prayer.ordinal(): Int = when (this) {
          Prayer.FAJR -> 0; Prayer.SUNRISE -> 1; Prayer.DHUHR -> 2
          Prayer.ASR -> 3; Prayer.MAGHRIB -> 4; Prayer.ISHA -> 5; else -> 99
      }

      private fun showNotification(title: String, message: String, id: Int, channelId: String = CHANNEL_PRAYER) {
          val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

          val icon = when (channelId) {
              CHANNEL_PRAYER -> android.R.drawable.btn_star_big_on
              CHANNEL_SECURITY -> android.R.drawable.stat_sys_warning
              CHANNEL_WISDOM -> android.R.drawable.ic_menu_info_details
              else -> android.R.drawable.stat_sys_warning
          }

          val category = when (channelId) {
              CHANNEL_PRAYER -> NotificationCompat.CATEGORY_ALARM
              CHANNEL_SECURITY -> NotificationCompat.CATEGORY_ERROR
              CHANNEL_WISDOM -> NotificationCompat.CATEGORY_RECOMMENDATION
              else -> NotificationCompat.CATEGORY_MESSAGE
          }

          val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
          val notification = NotificationCompat.Builder(applicationContext, channelId)
              .setSmallIcon(icon)
              .setContentTitle(title)
              .setContentText(message)
              .setStyle(NotificationCompat.BigTextStyle().bigText(message))
              .setPriority(
                  if (channelId == CHANNEL_WISDOM) NotificationCompat.PRIORITY_DEFAULT
                  else NotificationCompat.PRIORITY_MAX
              )
              .setCategory(category)
              .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
              .setSound(soundUri)
              .setVibrate(longArrayOf(0, 300, 100, 300, 100, 500))
              .setAutoCancel(true)
              .build()

          notificationManager.notify(id, notification)
      }
  }
  