package com.example

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.example.ui.design.AyriaDesignTokens
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaPanoramaBackdrop
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
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
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private var hasCompletedStartupGate = false
    private var isAuthInProgress       = false
    private var hasRequestedStartupAuthentication = false
    private var hasRequestedPermissions = false
    private var hasScheduledBackgroundWork = false

    companion object {
        @Volatile
        private var workManagerInitialized = false
    }

    /**
     * Keep the Android status bar visible and transparent while hiding only
     * the OS navigation bar. The app's own glass navigation remains available.
     */
    private fun applyImmersiveNavigationMode() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveNavigationMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.decorView.post { applyImmersiveNavigationMode() }
    }

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

        lifecycleScope.launch {
            try {
                // WorkManager auto-initialization is disabled in the manifest.
                // Initialize it here, only after the user reaches the dashboard.
                synchronized(MainActivity::class.java) {
                    if (!workManagerInitialized) {
                        WorkManager.initialize(
                            applicationContext,
                            androidx.work.Configuration.Builder().build()
                        )
                        workManagerInitialized = true
                    }
                }

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
        applyImmersiveNavigationMode()

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
                                var activeTab by rememberSaveable { mutableStateOf("home") }
                                val currentEntry by navController.currentBackStackEntryAsState()
                                val isDashboardRoute = currentEntry?.destination?.route == "dashboard"
                                BackHandler(enabled = isDashboardRoute && activeTab != "home") {
                                    activeTab = "home"
                                    vm.setSettingsOpen(false)
                                }

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
                                val panoramaRoll by vm.roll.collectAsState()
                                val panoramaPitch by vm.pitch.collectAsState()
                                val panoramaPage = when (activeTab) {
                                    "academy" -> 1
                                    "chat" -> 2
                                    "library" -> 3
                                    "settings" -> 4
                                    else -> 0
                                }
                                CompositionLocalProvider(LocalLayoutDirection provides if (isArLocal) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                                    Box(Modifier.fillMaxSize()) {
                                        AyriaPanoramaBackdrop(
                                            pageIndex = panoramaPage,
                                            roll = panoramaRoll,
                                            pitch = panoramaPitch,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
                                        containerColor = Color.Transparent,
                                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                    ) { innerPadding ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerPadding)
                                                .consumeWindowInsets(innerPadding)
                                                .padding(bottom = if (!windowInfo.usesNavigationRail) 96.dp else 0.dp)
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
                                                        "library"   -> {
                                                            val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()
                                                            LibraryScreen(vm, prayerVm) {
                                                                activeTab = "home"
                                                                vm.setSettingsOpen(false)
                                                            }
                                                        }
                                                        "chat"      -> {
                                                            val chatVm: GeminiChatViewModel = viewModel()
                                                            GeminiChatScreen(chatVm, uiConfig, isArLocal)
                                                        }
                                                        "settings"  -> SettingsScreen(
                                                            viewModel = vm,
                                                            onClose = { activeTab = "home"; vm.setSettingsOpen(false) },
                                                            onOpenAbout = { navController.navigate("about") },
                                                            onOpenCustomize = { navController.navigate("adaptive_feedback") },
                                                            onOpenAppLock = { navController.navigate("app_lock") }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!windowInfo.usesNavigationRail) {
                                        AnimatedVisibility(
                                            visible = navBarVisible,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .navigationBarsPadding()
                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 16.dp),
                                            enter = slideInVertically { it } + fadeIn(),
                                            exit = slideOutVertically { it } + fadeOut()
                                        ) {
                                            AppBottomNavBar(
                                                isArabic = isArLocal,
                                                activeTab = activeTab,
                                                onTabSelected = { tab ->
                                                    if (activeTab != tab) {
                                                        activeTab = tab
                                                        vm.setSettingsOpen(tab == "settings")
                                                    }
                                                },
                                                onOpenScanner = {
                                                    navController.navigate("link_scanner") { launchSingleTop = true }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    }
                                }
                            }
                            composable("link_scanner") {
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 0)
                                    AdaptiveContentFrame {
                                        LinkScannerScreen(vm) { navController.popBackStack() }
                                    }
                                }
                            }
                            composable("about") {
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 4)
                                    AdaptiveContentFrame {
                                        AboutScreen { navController.popBackStack() }
                                    }
                                }
                            }
                            composable("glossary") {
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 1)
                                    AdaptiveContentFrame {
                                        CyberGlossaryScreen(vm) { navController.popBackStack() }
                                    }
                                }
                            }
                            composable("checklist") {
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 1)
                                    AdaptiveContentFrame {
                                        SecurityChecklistScreen(vm) { navController.popBackStack() }
                                    }
                                }
                            }
                            composable("adaptive_feedback") {
                                val isArLocal by vm.isArabic.collectAsState()
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 4)
                                    AdaptiveContentFrame {
                                        FeedbackScreen(vm, isArLocal) { navController.popBackStack() }
                                    }
                                }
                            }
                            composable("app_lock") {
                                Box(Modifier.fillMaxSize()) {
                                    AyriaPanoramaBackdrop(pageIndex = 4)
                                    AdaptiveContentFrame {
                                        AppPinSettingsScreen(viewModel = vm) { navController.popBackStack() }
                                    }
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

private fun NavTab.icon(selected: Boolean) = when (key) {
    "home" -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    "academy" -> if (selected) Icons.Filled.School else Icons.Outlined.School
    "chat" -> if (selected) Icons.Filled.SmartToy else Icons.Outlined.SmartToy
    "library" -> if (selected) Icons.Filled.AutoStories else Icons.Outlined.AutoStories
    else -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
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
private fun AppNavigationRail(isArabic: Boolean, activeTab: String, onTabSelected: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    NavigationRail(containerColor = Color(0xFF02050D).copy(alpha = 0.98f)) {
        Spacer(Modifier.height(12.dp))
        appNavigationTabs.forEach { tab ->
            val selected = activeTab == tab.key
            val color = tab.accentColor()
            NavigationRailItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTabSelected(tab.key)
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = tab.label(isArabic),
                        tint = if (selected) color else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = null,
                alwaysShowLabel = false,
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
internal fun AppBottomNavBar(
    isArabic: Boolean,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val selectedIndex = appNavigationTabs.indexOfFirst { it.key == activeTab }.coerceAtLeast(0)
    val capsuleShape = RoundedCornerShape(38.dp)
    val actionWidth = 62.dp
    val horizontalInset = 6.dp

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .widthIn(max = 440.dp)
                .height(76.dp)
                .shadow(22.dp, capsuleShape, clip = false)
                .clip(capsuleShape)
                .background(
                    Brush.verticalGradient(listOf(Color(0xE51A3653), Color(0xF20A1C31), Color(0xEA071629)))
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            AyriaDesignTokens.Colors.glacier.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.07f)
                        )
                    ),
                    capsuleShape
                )
        ) {
            val availableWidth = maxWidth - horizontalInset * 2f
            val slotWidth = ((availableWidth - actionWidth) / 5f).coerceAtLeast(1.dp)
            val leftGroupCount = if (isArabic) 2 else 3
            val physicalSlot = if (isArabic) 4 - selectedIndex else selectedIndex
            val centerX = if (physicalSlot < leftGroupCount) {
                slotWidth * (physicalSlot.toFloat() + 0.5f)
            } else {
                slotWidth * leftGroupCount.toFloat() + actionWidth + slotWidth * (physicalSlot - leftGroupCount + 0.5f)
            }
            val indicatorWidth = (slotWidth * 1.78f).coerceIn(72.dp, 104.dp)
            val targetOffsetPx = with(density) { (centerX - indicatorWidth / 2f).toPx() }
            val indicatorOffset = remember { Animatable(targetOffsetPx) }
            val indicatorScale = remember { Animatable(1f) }
            var indicatorPlaced by remember { mutableStateOf(false) }

            LaunchedEffect(activeTab, targetOffsetPx) {
                if (!indicatorPlaced) {
                    indicatorOffset.snapTo(targetOffsetPx)
                    indicatorPlaced = true
                } else {
                    kotlinx.coroutines.coroutineScope {
                        launch {
                            indicatorOffset.animateTo(
                                targetOffsetPx,
                                spring(dampingRatio = 0.78f, stiffness = 480f)
                            )
                        }
                        launch {
                            indicatorScale.snapTo(0.94f)
                            indicatorScale.animateTo(1.08f, spring(dampingRatio = 0.55f, stiffness = 420f))
                            indicatorScale.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = 520f))
                        }
                    }
                }
            }

            val pulse = rememberInfiniteTransition(label = "shield_pulse")
            val ringScale by pulse.animateFloat(
                initialValue = 0.88f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "shield_ring_scale"
            )
            val ringAlpha by pulse.animateFloat(
                initialValue = 0.34f, targetValue = 0.08f,
                animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "shield_ring_alpha"
            )

            Box(Modifier.fillMaxSize().padding(horizontal = horizontalInset, vertical = 6.dp)) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.value.roundToInt(), 0) }
                        .width(indicatorWidth)
                        .height(64.dp)
                        .align(Alignment.CenterStart)
                        .graphicsLayer { scaleX = indicatorScale.value; scaleY = 1f }
                        .shadow(8.dp, RoundedCornerShape(30.dp), clip = false)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xAA8AD9F4), Color(0x664B9CC5), Color(0x33214B70))))
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.52f),
                                    AyriaDesignTokens.Colors.glacier.copy(alpha = 0.24f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            RoundedCornerShape(30.dp)
                        )
                )

                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    val leftTabs = if (isArabic) appNavigationTabs.drop(3).reversed() else appNavigationTabs.take(3)
                    val rightTabs = if (isArabic) appNavigationTabs.take(3).reversed() else appNavigationTabs.drop(3)

                    Row(
                        Modifier.weight(leftTabs.size.toFloat()).fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        leftTabs.forEach { tab ->
                            GlassNavigationItem(
                                tab, isArabic, activeTab == tab.key,
                                Modifier.weight(1f).fillMaxHeight()
                            ) {
                                if (activeTab != tab.key) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTabSelected(tab.key)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(actionWidth)
                            .fillMaxHeight()
                            .semantics {
                                contentDescription = if (isArabic) "فحص المصادر والحماية" else "Security scan"
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenScanner()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(62.dp).background(
                                Brush.radialGradient(
                                    listOf(
                                        AyriaDesignTokens.Colors.teal.copy(alpha = 0.28f),
                                        AyriaDesignTokens.Colors.ion.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                        )
                        Canvas(Modifier.size(60.dp).graphicsLayer {
                            scaleX = ringScale; scaleY = ringScale; alpha = ringAlpha
                        }) {
                            drawCircle(
                                color = AyriaDesignTokens.Colors.glacier,
                                radius = size.minDimension / 2f - 1.dp.toPx(),
                                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Box(
                            Modifier
                                .size(48.dp)
                                .shadow(12.dp, CircleShape, clip = false)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF8DEBFF), Color(0xFF2D9BC9), Color(0xFF164F83))))
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.80f), Color.White.copy(alpha = 0.16f))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFF3FCFF), modifier = Modifier.size(27.dp))
                        }
                    }

                    Row(
                        Modifier.weight(rightTabs.size.toFloat()).fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rightTabs.forEach { tab ->
                            GlassNavigationItem(
                                tab, isArabic, activeTab == tab.key,
                                Modifier.weight(1f).fillMaxHeight()
                            ) {
                                if (activeTab != tab.key) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTabSelected(tab.key)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassNavigationItem(
    tab: NavTab,
    isArabic: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = tab.label(isArabic)
    CompositionLocalProvider(LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .semantics(mergeDescendants = true) {
                    contentDescription = label
                    this.selected = selected
                }
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.icon(selected),
                contentDescription = null,
                tint = if (selected) Color(0xFFF0FCFF) else Color(0xFFB7D0E2).copy(alpha = 0.76f),
                modifier = Modifier.size(if (selected) 21.dp else 23.dp)
            )
            if (selected) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    color = Color(0xFFF0FCFF),
                    fontFamily = AyriaArabicFont,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
