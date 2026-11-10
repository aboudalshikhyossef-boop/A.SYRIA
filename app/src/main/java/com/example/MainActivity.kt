package com.example

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.work.*
import com.example.worker.NotificationWorker
import java.util.concurrent.TimeUnit
import com.example.adaptive.LocalAdaptiveConfig
import com.example.adaptive.AdaptiveContentFrame
import com.example.adaptive.LocalWindowAdaptiveInfo
import com.example.adaptive.ProvideWindowAdaptiveLayout
import com.example.ui.screens.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.AmberZen
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.GeminiChatViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private var hasCompletedStartupGate = false
    private var isAuthInProgress       = false
    private var hasRequestedStartupAuthentication = false
    private var hasRequestedPermissions = false
    private var hasScheduledBackgroundWork = false
    private var pendingCredentialSuccess: (() -> Unit)? = null
    private var pendingCredentialOnCancel: (() -> Unit)? = null
    private var pendingCredentialIsStartupGate = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    private val deviceCredentialLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val onSuccess = pendingCredentialSuccess
            val onCancel = pendingCredentialOnCancel
            val isStartupGate = pendingCredentialIsStartupGate
            pendingCredentialSuccess = null
            pendingCredentialOnCancel = null
            pendingCredentialIsStartupGate = false
            isAuthInProgress = false

            if (result.resultCode == Activity.RESULT_OK) {
                if (isStartupGate) {
                    hasCompletedStartupGate = true
                    requestEssentialPermissionsOnce()
                }
                onSuccess?.invoke()
            } else {
                android.util.Log.i("A.SYRIA.Auth", "Device credential flow cancelled by the user")
                onCancel?.invoke()
            }
        }

    @Suppress("DEPRECATION")
    private fun launchLegacyDeviceCredential(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        closeOnCancel: Boolean,
        onCancel: (() -> Unit)? = null
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return false

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) return false

        val credentialIntent = keyguardManager.createConfirmDeviceCredentialIntent(title, subtitle)
            ?: return false

        return runCatching {
            pendingCredentialSuccess = onSuccess
            pendingCredentialOnCancel = onCancel
            pendingCredentialIsStartupGate = closeOnCancel
            isAuthInProgress = true
            deviceCredentialLauncher.launch(credentialIntent)
            true
        }.getOrElse {
            pendingCredentialSuccess = null
            pendingCredentialOnCancel = null
            pendingCredentialIsStartupGate = false
            isAuthInProgress = false
            false
        }
    }

    private fun scheduleBackgroundWorkSafely() {
        if (hasScheduledBackgroundWork) return
        hasScheduledBackgroundWork = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val workRequest =
                    PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "sovereign_pulse",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
                android.util.Log.i("A.SYRIA.Startup", "Optional notification work scheduled")
            } catch (error: Exception) {
                // Notifications are optional; WorkManager initialization must not close the app.
                android.util.Log.e(
                    "A.SYRIA.Startup",
                    "Optional WorkManager scheduling failed; continuing without background notifications",
                    error
                )
            }
        }
    }

    private fun requestEssentialPermissionsOnce() {
        if (hasRequestedPermissions) return
        hasRequestedPermissions = true
        requestEssentialPermissions()
    }

    private fun requestEssentialPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    /**
     * Device authentication is used only after the user explicitly selects
     * "Forgot app PIN". It never gates an ordinary app launch.
     */
    private fun authenticateForPinRecovery(
        isArabic: Boolean,
        onAuthenticated: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (isAuthInProgress || isFinishing || isDestroyed) return

        val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val hasSecureDeviceCredential = runCatching { keyguard.isDeviceSecure }.getOrDefault(false)
        if (!hasSecureDeviceCredential) {
            onFailure(
                if (isArabic) "لا يوجد قفل شاشة آمن على الهاتف. أضف PIN أو نمطاً أو كلمة مرور إلى الهاتف ثم أعد الاسترداد."
                else "No secure phone screen lock is configured. Set a phone PIN, pattern, or password before recovering the app PIN."
            )
            return
        }

        val biometricManager = BiometricManager.from(this)
        val hasStrongBiometric = runCatching {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
        }.getOrDefault(false)

        val title = if (isArabic) "استرداد قفل A.SYRIA" else "Recover A.SYRIA app lock"
        val subtitle = if (isArabic) "أكد هويتك باستخدام وسيلة حماية الهاتف" else "Verify with your phone's secure authentication"

        fun launchLegacyCredential(): Boolean {
            return launchLegacyDeviceCredential(
                title = title,
                subtitle = subtitle,
                onSuccess = onAuthenticated,
                closeOnCancel = false,
                onCancel = {
                    onFailure(
                        if (isArabic) "تم إلغاء استرداد الرمز. ما زال التطبيق مقفلاً."
                        else "Recovery was cancelled. The app remains locked."
                    )
                }
            )
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !hasStrongBiometric) {
            if (!launchLegacyCredential()) {
                onFailure(
                    if (isArabic) "تعذّر فتح شاشة قفل الهاتف. تحقق من إعداد PIN أو النمط أو كلمة المرور."
                    else "Could not open the phone credential screen. Check its PIN, pattern, or password settings."
                )
            }
            return
        }

        val allowedAuthenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL or
                if (hasStrongBiometric) BiometricManager.Authenticators.BIOMETRIC_STRONG else 0
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        try {
            val prompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        isAuthInProgress = false
                        if (
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            if (!launchLegacyCredential()) {
                                onFailure(
                                    if (isArabic) "تعذّر فتح مصادقة الهاتف الاحتياطية."
                                    else "The phone-credential fallback could not be opened."
                                )
                            }
                        } else {
                            onFailure(
                                if (isArabic) "لم تكتمل المصادقة. أعد المحاولة أو استخدم رمز الهاتف."
                                else "Authentication did not complete. Retry or use your phone credential."
                            )
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        isAuthInProgress = false
                        onAuthenticated()
                    }

                    override fun onAuthenticationFailed() {
                        android.util.Log.w("A.SYRIA.Auth", "Recovery biometric sample was not recognized")
                    }
                }
            )

            val infoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(allowedAuthenticators)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                infoBuilder.setNegativeButtonText(
                    if (isArabic) "استخدام رمز الهاتف" else "Use phone PIN"
                )
            }

            isAuthInProgress = true
            prompt.authenticate(infoBuilder.build())
        } catch (error: Exception) {
            isAuthInProgress = false
            android.util.Log.e("A.SYRIA.Auth", "Could not launch device authentication for app PIN recovery", error)
            if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                !launchLegacyCredential()
            ) {
                onFailure(
                    if (isArabic) "تعذّر تشغيل مصادقة الهاتف. أعد المحاولة."
                    else "Phone authentication could not start. Please retry."
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                onFailure(
                    if (isArabic) "تعذّر تشغيل مصادقة الهاتف. أعد المحاولة."
                    else "Phone authentication could not start. Please retry."
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        android.util.Log.i("A.SYRIA.Startup", "MainActivity.onCreate started on API ${Build.VERSION.SDK_INT}")

        setContent {
            val vm: DashboardViewModel = viewModel()
            LaunchedEffect(Unit) {
                android.util.Log.i("A.SYRIA.Startup", "Compose root attached; waiting for initial preferences")
            }

            val uiConfig             by vm.uiConfig.collectAsState()
            val isAr                 by vm.isArabic.collectAsState()
            val calibrationCompleted by vm.calibrationCompleted.collectAsState()
            val onboardingCompleted  by vm.onboardingCompleted.collectAsState()
            val isDataLoaded         by vm.isDataLoaded.collectAsState()
            val appPinSettingsLoaded by vm.isAppPinSettingsLoaded.collectAsState()
            val appPinEnabled        by vm.isAppPinEnabled.collectAsState()
            val appPinUnlocked       by vm.isAppPinUnlocked.collectAsState()
            val appPinFailedAttempts by vm.appPinFailedAttempts.collectAsState()
            val appPinLockoutUntil   by vm.appPinLockoutUntilMillis.collectAsState()
            var appPinRecoveryMode by remember { mutableStateOf(false) }
            val customApiKey         by vm.customApiKey.collectAsState()
            var startupGateUnlocked by remember { mutableStateOf(false) }
            var startupGateMessage by remember { mutableStateOf<String?>(null) }

            MyApplicationTheme(darkTheme = true, dynamicColor = false, uiConfig = uiConfig) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    fun triggerBiometricAuth(title: String, subtitle: String, onSuccess: () -> Unit, closeOnCancel: Boolean = false) {
                        if (isAuthInProgress || isFinishing || isDestroyed) return

                        val apiLevel = Build.VERSION.SDK_INT
                        val supportsDeviceCredentialInPrompt = apiLevel >= Build.VERSION_CODES.R
                        val biometricAuthenticator = BiometricManager.Authenticators.BIOMETRIC_STRONG
                        val deviceCredentialAuthenticator = BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        val biometricManager = BiometricManager.from(this@MainActivity)
                        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                        val hasSecureDeviceCredential = runCatching { keyguardManager.isDeviceSecure }.getOrDefault(false)

                        val hasStrongBiometric = try {
                            biometricManager.canAuthenticate(biometricAuthenticator) == BiometricManager.BIOMETRIC_SUCCESS
                        } catch (error: RuntimeException) {
                            android.util.Log.e("A.SYRIA.Auth", "Biometric availability check failed on API $apiLevel", error)
                            false
                        }

                        if (!supportsDeviceCredentialInPrompt && !hasStrongBiometric && hasSecureDeviceCredential) {
                            val legacyCredentialLaunched = launchLegacyDeviceCredential(
                                title,
                                subtitle,
                                onSuccess,
                                closeOnCancel,
                                onCancel = if (closeOnCancel) {
                                    { startupGateMessage = if (isAr) "تم إلغاء إدخال رمز قفل الجهاز. أعد المحاولة أو اختر المتابعة دون قفل." else "Device credential entry was cancelled. Retry or continue without the app lock." }
                                } else null
                            )
                            if (legacyCredentialLaunched) return

                            // A configured PIN/pattern exists. If Android cannot open its credential
                            // screen, keep the app locked instead of silently disabling the gate.
                            startupGateMessage = if (isAr) {
                                "تعذّر فتح شاشة PIN أو النمط في هذا الجهاز. أعد المحاولة أو اختر المتابعة دون قفل."
                            } else {
                                "Android could not open the device PIN/pattern screen. Retry or explicitly continue without the app lock."
                            }
                            Toast.makeText(this@MainActivity, startupGateMessage, Toast.LENGTH_LONG).show()
                            return
                        }

                        val allowedAuthenticators = when {
                            supportsDeviceCredentialInPrompt && hasSecureDeviceCredential && hasStrongBiometric ->
                                biometricAuthenticator or deviceCredentialAuthenticator
                            supportsDeviceCredentialInPrompt && hasSecureDeviceCredential ->
                                deviceCredentialAuthenticator
                            hasStrongBiometric ->
                                biometricAuthenticator
                            else -> 0
                        }

                        if (allowedAuthenticators == 0) {
                            val message = if (isAr) {
                                "هذا الجهاز لا يحتوي على بصمة مسجلة أو قفل شاشة آمن. تم إيقاف قفل التطبيق عند الفتح حتى تتمكن من استخدام A.SYRIA."
                            } else {
                                "This device has no enrolled biometric or secure screen lock. App launch lock has been disabled so you can use A.SYRIA."
                            }
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()

                            if (closeOnCancel) {
                                hasCompletedStartupGate = true
                                startupGateUnlocked = true
                                vm.setLockAppOnLaunch(false)
                                requestEssentialPermissionsOnce()
                            }
                            return
                        }

                        fun launchPrompt(authenticators: Int) {
                            val prompt = BiometricPrompt(
                                this@MainActivity,
                                ContextCompat.getMainExecutor(this@MainActivity),
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationError(code: Int, msg: CharSequence) {
                                        isAuthInProgress = false
                                        android.util.Log.w(
                                            "A.SYRIA.Auth",
                                            "Authentication ended with code=$code on API $apiLevel: $msg"
                                        )

                                        if (
                                            code == BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                            !supportsDeviceCredentialInPrompt &&
                                            hasSecureDeviceCredential
                                        ) {
                                            if (
                                                !launchLegacyDeviceCredential(
                                                    title,
                                                    subtitle,
                                                    onSuccess,
                                                    closeOnCancel,
                                                    onCancel = if (closeOnCancel) {
                                                        { startupGateMessage = if (isAr) "تم إلغاء المصادقة برمز الجهاز. أعد المحاولة أو اختر المتابعة دون قفل." else "Device credential authentication was cancelled. Retry or continue without the app lock." }
                                                    } else null
                                                ) && closeOnCancel
                                            ) {
                                                startupGateMessage = if (isAr) "تعذّر فتح قفل الجهاز. أعد المحاولة." else "The device credential screen could not be opened. Please retry."
                                            }
                                        } else if (closeOnCancel) {
                                            startupGateMessage = if (code == BiometricPrompt.ERROR_USER_CANCELED) {
                                                if (isAr) "ألغيت المصادقة. التطبيق ما زال مقفلًا؛ أعد المحاولة أو اختر المتابعة دون قفل."
                                                else "Authentication was cancelled. The app remains locked; retry or continue without the app lock."
                                            } else {
                                                msg.toString().ifBlank {
                                                    if (isAr) "لم تكتمل المصادقة. أعد المحاولة." else "Authentication did not complete. Please retry."
                                                }
                                            }
                                        }
                                    }

                                    override fun onAuthenticationFailed() {
                                        android.util.Log.w("A.SYRIA.Auth", "Biometric sample was not recognized")
                                    }

                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        isAuthInProgress = false
                                        if (closeOnCancel) {
                                            hasCompletedStartupGate = true
                                            startupGateUnlocked = true
                                            startupGateMessage = null
                                            requestEssentialPermissionsOnce()
                                        }
                                        onSuccess()
                                    }
                                }
                            )

                            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                                .setTitle(title)
                                .setSubtitle(subtitle)
                                .setAllowedAuthenticators(authenticators)

                            if (
                                !supportsDeviceCredentialInPrompt &&
                                authenticators == biometricAuthenticator
                            ) {
                                promptInfoBuilder.setNegativeButtonText(
                                    if (hasSecureDeviceCredential) {
                                        if (isAr) "استخدام رمز قفل الجهاز" else "Use device PIN"
                                    } else {
                                        if (isAr) "إلغاء" else "Cancel"
                                    }
                                )
                            }

                            prompt.authenticate(promptInfoBuilder.build())
                        }

                        isAuthInProgress = true
                        try {
                            launchPrompt(allowedAuthenticators)
                        } catch (error: Exception) {
                            isAuthInProgress = false
                            android.util.Log.e(
                                "A.SYRIA.Auth",
                                "Failed to build or launch authentication prompt on API $apiLevel",
                                error
                            )

                            if (
                                supportsDeviceCredentialInPrompt &&
                                hasSecureDeviceCredential &&
                                allowedAuthenticators != deviceCredentialAuthenticator
                            ) {
                                isAuthInProgress = true
                                try {
                                    launchPrompt(deviceCredentialAuthenticator)
                                    return
                                } catch (fallbackError: Exception) {
                                    isAuthInProgress = false
                                    android.util.Log.e("A.SYRIA.Auth", "Device credential fallback failed", fallbackError)
                                }
                            }

                            if (
                                !supportsDeviceCredentialInPrompt &&
                                hasSecureDeviceCredential &&
                                launchLegacyDeviceCredential(
                                    title,
                                    subtitle,
                                    onSuccess,
                                    closeOnCancel,
                                    onCancel = if (closeOnCancel) {
                                        { startupGateMessage = if (isAr) "تم إلغاء المصادقة. التطبيق ما زال مقفلًا." else "Authentication was cancelled. The app remains locked." }
                                    } else null
                                )
                            ) return

                            startupGateMessage = if (isAr) {
                                "تعذّر فتح نافذة المصادقة. أعد المحاولة أو اختر المتابعة دون قفل."
                            } else {
                                "The authentication prompt could not be opened. Retry or continue without the app lock."
                            }
                            Toast.makeText(
                                this@MainActivity,
                                startupGateMessage ?: if (isAr) "تعذّر فتح نافذة المصادقة." else "Unable to open the authentication prompt.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


                    DisposableEffect(appPinEnabled) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_STOP && !isAuthInProgress) {
                                vm.lockAppSession()
                            }
                        }
                        lifecycle.addObserver(observer)
                        onDispose { lifecycle.removeObserver(observer) }
                    }

                    // Wait for DataStore to load before computing startDestination
                    // This prevents the onboarding from showing every time
                    val startDestination = remember(isDataLoaded, onboardingCompleted, calibrationCompleted) {
                        if (!isDataLoaded) "splash" // Stay on splash while loading
                        else when {
                            !onboardingCompleted  -> "onboarding"
                            !calibrationCompleted -> "calibration"
                            else                  -> "dashboard"
                        }
                    }

                    CompositionLocalProvider(LocalAdaptiveConfig provides uiConfig) {
                        ProvideWindowAdaptiveLayout {
                        Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(navController = navController, startDestination = "splash", enterTransition = { fadeIn(animationSpec = tween(500)) }, exitTransition = { fadeOut(animationSpec = tween(500)) }) {
                            composable("splash") {
                                var splashFinished by remember { mutableStateOf(false) }

                                LaunchedEffect(splashFinished, startDestination) {
                                    if (splashFinished && startDestination != "splash") {
                                        android.util.Log.i("A.SYRIA.Startup", "Splash completed; navigating to $startDestination")
                                        navController.navigate(startDestination) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }

                                SplashScreen(isArabic = isAr) {
                                    splashFinished = true
                                }
                            }
                            composable("onboarding") {
                                OnboardingScreen(isArabic = isAr) { profile ->
                                    vm.completeOnboarding()
                                    if (profile != null) vm.saveUserProfile(profile.gender, profile.birthYear)
                                    navController.navigate("calibration") { popUpTo("onboarding") { inclusive = true } }
                                }
                            }
                            composable("calibration") {
                                CalibrationScreen(viewModel = vm) {
                                    navController.navigate("dashboard") { popUpTo("calibration") { inclusive = true } }
                                }
                            }
                            composable("dashboard") {
                                val isArLocal by vm.isArabic.collectAsState()
                                var activeTab by remember { mutableStateOf("home") }

                                DisposableEffect(vm, appPinEnabled, appPinUnlocked) {
                                    fun startDashboardServices() {
                                        if (appPinEnabled && !appPinUnlocked) return
                                        runCatching {
                                            vm.startTelemetry()
                                            vm.startSensors()
                                        }.onFailure { error ->
                                            android.util.Log.e("A.SYRIA.Startup", "Dashboard telemetry could not start", error)
                                        }
                                    }

                                    val observer = LifecycleEventObserver { _, event ->
                                        when (event) {
                                            Lifecycle.Event.ON_RESUME -> startDashboardServices()
                                            Lifecycle.Event.ON_PAUSE -> vm.stopSensors()
                                            Lifecycle.Event.ON_STOP -> {
                                                vm.stopSensors()
                                                vm.stopTelemetry()
                                            }
                                            else -> Unit
                                        }
                                    }
                                    lifecycle.addObserver(observer)

                                    // ON_RESUME may have happened before this observer was attached
                                    // during an in-app navigation. Start explicitly for that case.
                                    startDashboardServices()

                                    onDispose {
                                        lifecycle.removeObserver(observer)
                                        vm.stopSensors()
                                        vm.stopTelemetry()
                                    }
                                }

                                LaunchedEffect(appPinEnabled, appPinUnlocked) {
                                    if (!appPinEnabled || appPinUnlocked) {
                                        requestEssentialPermissionsOnce()
                                        scheduleBackgroundWorkSafely()
                                    } else {
                                        vm.stopSensors()
                                        vm.stopTelemetry()
                                    }
                                }
                                val isAcademyOpen   by vm.isAcademyOpen.collectAsState()
                                val isResourcesOpen by vm.isResourcesOpen.collectAsState()

                                LaunchedEffect(isAcademyOpen)   { if (isAcademyOpen)   { activeTab = "academy";   vm.setAcademyOpen(false) } }
                                LaunchedEffect(isResourcesOpen) { if (isResourcesOpen) { activeTab = "resources"; vm.setResourcesOpen(false) } }

                                // Only redirect to settings if API key is KNOWN to be blank (after data loaded)
                                LaunchedEffect(isDataLoaded, customApiKey) {
                                    if (isDataLoaded && customApiKey.isBlank()) {
                                        activeTab = "settings"; vm.setSettingsOpen(true)
                                    }
                                }
                                LaunchedEffect(customApiKey, calibrationCompleted) {
                                    if (customApiKey.isNotBlank() && !calibrationCompleted) {
                                        navController.navigate("calibration") { popUpTo("dashboard") { inclusive = false } }
                                    }
                                }

                                // Auto-hide nav bar on scroll
                                var navBarVisible by remember { mutableStateOf(true) }
                                val nestedScrollConnection = remember {
                                    object : NestedScrollConnection {
                                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                            navBarVisible = available.y > 0 || available.y == 0f
                                            return Offset.Zero
                                        }
                                    }
                                }

                                val windowInfo = LocalWindowAdaptiveInfo.current
                                CompositionLocalProvider(LocalLayoutDirection provides if (isArLocal) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
                                        containerColor = VoidBlack,
                                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                        bottomBar = {
                                            if (!windowInfo.usesNavigationRail) {
                                                AnimatedVisibility(visible = navBarVisible, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                                                    AppBottomNavBar(isArabic = isArLocal, activeTab = activeTab) { tab ->
                                                        activeTab = tab
                                                        vm.setSettingsOpen(tab == "settings")
                                                    }
                                                }
                                            }
                                        }
                                    ) { innerPadding ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerPadding)
                                                .consumeWindowInsets(innerPadding)
                                        ) {
                                            if (windowInfo.usesNavigationRail) {
                                                AppNavigationRail(isArabic = isArLocal, activeTab = activeTab) { tab ->
                                                    activeTab = tab
                                                    vm.setSettingsOpen(tab == "settings")
                                                }
                                            }

                                            Box(
                                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                                contentAlignment = Alignment.TopCenter
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .widthIn(max = windowInfo.maxContentWidth)
                                                        .fillMaxSize()
                                                ) {
                                                    when (activeTab) {
                                                        "home"      -> {
                                                            if (!appPinEnabled || appPinUnlocked) {
                                                                val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()
                                                                DashboardScreen(vm, prayerVm, { navController.navigate("link_scanner") }, { t, s, ok -> triggerBiometricAuth(t, s, ok) })
                                                            }
                                                        }
                                                        "academy"   -> AcademyScreen(vm, onNavigateToGlossary = { navController.navigate("glossary") }, onNavigateToChecklist = { navController.navigate("checklist") })
                                                        "resources" -> ResourcesScreen(vm) { activeTab = "home" }
                                                        "library"   -> LibraryScreen(vm) { activeTab = "home" }
                                                        "chat"      -> {
                                                            val chatVm: GeminiChatViewModel = viewModel()
                                                            GeminiChatScreen(chatVm, uiConfig, isArLocal)
                                                        }
                                                        "settings"  -> SettingsScreen(
                                                            viewModel = vm,
                                                            onClose = { activeTab = "home" },
                                                            onOpenAbout = { navController.navigate("about") },
                                                            onOpenCustomize = { navController.navigate("adaptive_feedback") },
                                                            onOpenAppLock = { navController.navigate("app_lock") }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            composable("link_scanner") {
                                AdaptiveContentFrame {
                                    LinkScannerScreen(vm) { navController.popBackStack() }
                                }
                            }
                            composable("about") {
                                AdaptiveContentFrame {
                                    AboutScreen { navController.popBackStack() }
                                }
                            }
                            composable("glossary") {
                                AdaptiveContentFrame {
                                    CyberGlossaryScreen(vm) { navController.popBackStack() }
                                }
                            }
                            composable("checklist") {
                                AdaptiveContentFrame {
                                    SecurityChecklistScreen(vm) { navController.popBackStack() }
                                }
                            }
                            composable("adaptive_feedback") {
                                val isArLocal by vm.isArabic.collectAsState()
                                AdaptiveContentFrame {
                                    FeedbackScreen(vm, isArLocal) { navController.popBackStack() }
                                }
                            }
                            composable("app_lock") {
                                AdaptiveContentFrame {
                                    AppPinSettingsScreen(viewModel = vm) { navController.popBackStack() }
                                }
                            }
                        }


                        if (
                            isDataLoaded &&
                            appPinSettingsLoaded &&
                            appPinEnabled &&
                            !appPinUnlocked
                        ) {
                            AppPinLockScreen(
                                isArabic = isAr,
                                failedAttempts = appPinFailedAttempts,
                                lockoutUntilMillis = appPinLockoutUntil,
                                recoveryMode = appPinRecoveryMode,
                                onVerify = { pin, result ->
                                    vm.verifyAppPin(pin, result)
                                },
                                onForgotPin = {
                                    authenticateForPinRecovery(
                                        isArabic = isAr,
                                        onAuthenticated = { appPinRecoveryMode = true },
                                        onFailure = { message ->
                                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onResetPin = { pin, result ->
                                    vm.enableAppPin(pin) { saved ->
                                        if (saved) appPinRecoveryMode = false
                                        result(saved)
                                    }
                                }
                            )
                        }
                        }
                        }
                        }
                    }
                }
            }
        }
    }

private data class NavTab(val key: String, val labelEn: String, val labelAr: String)

private val appNavigationTabs = listOf(
    NavTab("home", "Home", "الرئيسية"),
    NavTab("academy", "Academy", "الأكاديمية"),
    NavTab("chat", "Chat", "الدردشة"),
    NavTab("library", "Library", "المكتبة"),
    NavTab("settings", "Settings", "الإعدادات")
)

private fun NavTab.icon() = when (key) {
    "home" -> Icons.Rounded.Home
    "academy" -> Icons.Rounded.School
    "chat" -> Icons.Rounded.SmartToy
    "resources" -> Icons.AutoMirrored.Rounded.MenuBook
    "library" -> Icons.Rounded.AutoStories
    else -> Icons.Rounded.Settings
}

private fun NavTab.accentColor() = when (key) {
    "home" -> CyberCyan
    "academy" -> Color(0xFF00FF88)
    "chat" -> Color(0xFFBB66FF)
    "resources" -> AmberZen
    "library" -> Color(0xFFAA77FF)
    else -> Color(0xFFFF6B35)
}

private fun NavTab.label(isArabic: Boolean) = if (isArabic) labelAr else labelEn

@Composable
private fun AppNavigationRail(
    isArabic: Boolean,
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    NavigationRail(containerColor = Color(0xFF02050D).copy(alpha = 0.98f)) {
        Spacer(Modifier.height(12.dp))
        appNavigationTabs.forEach { tab ->
            val selected = activeTab == tab.key
            val color = tab.accentColor()
            NavigationRailItem(
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onTabSelected(tab.key)
                },
                icon = {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = tab.label(isArabic),
                        tint = if (selected) color else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label(isArabic),
                        color = if (selected) color else Color.White.copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = color,
                    selectedTextColor = color,
                    unselectedIconColor = Color.White.copy(alpha = 0.35f),
                    unselectedTextColor = Color.White.copy(alpha = 0.45f),
                    indicatorColor = color.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
private fun AppBottomNavBar(isArabic: Boolean, activeTab: String, onTabSelected: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "nav_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            CyberCyan.copy(alpha = glowAlpha * 0.5f),
                            AmberZen.copy(alpha = glowAlpha * 0.3f),
                            CyberCyan.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        NavigationBar(
            containerColor = Color(0xFF02050D).copy(alpha = 0.98f),
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            appNavigationTabs.forEach { tab ->
                val selected = activeTab == tab.key
                val color = tab.accentColor()
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        haptic.performHapticFeedback(
                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                        )
                        onTabSelected(tab.key)
                    },
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(color.copy(alpha = glowAlpha * 0.12f), RoundedCornerShape(14.dp))
                                        .border(1.dp, color.copy(alpha = glowAlpha * 0.5f), RoundedCornerShape(14.dp))
                                )
                            }
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = tab.label(isArabic),
                                tint = if (selected) color else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label(isArabic),
                            color = if (selected) color else Color.White.copy(alpha = 0.25f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    }
}
