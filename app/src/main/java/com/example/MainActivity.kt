package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.ui.viewmodel.GroqChatViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : FragmentActivity() {

    private var isSessionAuthenticated = false
    private var isAuthInProgress       = false
    private var hasRequestedPermissions = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    private fun requestEssentialPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "sovereign_pulse", ExistingPeriodicWorkPolicy.UPDATE, workRequest
        )

        setContent {
            val vm: DashboardViewModel    = viewModel()
            val chatVm: GroqChatViewModel = viewModel()
            val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()

            val uiConfig             by vm.uiConfig.collectAsState()
            val isAr                 by vm.isArabic.collectAsState()
            val calibrationCompleted by vm.calibrationCompleted.collectAsState()
            val onboardingCompleted  by vm.onboardingCompleted.collectAsState()
            val isDataLoaded         by vm.isDataLoaded.collectAsState()
            val customApiKey         by vm.customApiKey.collectAsState()

            MyApplicationTheme(darkTheme = true, dynamicColor = false, uiConfig = uiConfig) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    fun triggerBiometricAuth(title: String, subtitle: String, onSuccess: () -> Unit, closeOnCancel: Boolean = false) {
                        if (isAuthInProgress) return
                        val manager = BiometricManager.from(this@MainActivity)
                        if (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
                            isSessionAuthenticated = true; onSuccess(); return
                        }
                        isAuthInProgress = true
                        val executor = ContextCompat.getMainExecutor(this@MainActivity)
                        val prompt = BiometricPrompt(this@MainActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(code: Int, msg: CharSequence) { isAuthInProgress = false; if ((code == BiometricPrompt.ERROR_USER_CANCELED || code == BiometricPrompt.ERROR_NEGATIVE_BUTTON) && closeOnCancel) finish() }
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { isAuthInProgress = false; isSessionAuthenticated = true; onSuccess() }
                        })
                        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle).setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build())
                    }

                    DisposableEffect(Unit) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> { if (!isSessionAuthenticated && !isAuthInProgress) triggerBiometricAuth(if (isAr) "تأكيد الهوية" else "Identity Verified", if (isAr) "مطلوب بصمة الدخول" else "Biometric uplink required", { }, true) }
                                Lifecycle.Event.ON_RESUME -> { vm.startSensors(); if (isSessionAuthenticated) { prayerVm.updateLocation(); if (!hasRequestedPermissions) { hasRequestedPermissions = true; requestEssentialPermissions() } } }
                                Lifecycle.Event.ON_PAUSE  -> vm.stopSensors()
                                else -> {}
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
                        NavHost(navController = navController, startDestination = "splash", enterTransition = { fadeIn(animationSpec = tween(500)) }, exitTransition = { fadeOut(animationSpec = tween(500)) }) {
                            composable("splash") {
                                SplashScreen {
                                    // SplashScreen uses rememberUpdatedState so this always gets latest startDestination
                                    if (startDestination != "splash") {
                                        navController.navigate(startDestination) { popUpTo("splash") { inclusive = true } }
                                    }
                                    // If still loading, splash will keep running until DataStore loads and callback is refreshed
                                }
                            }
                            composable("onboarding") {
                                OnboardingScreen { profile ->
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
                                val isAcademyOpen   by vm.isAcademyOpen.collectAsState()
                                val isResourcesOpen by vm.isResourcesOpen.collectAsState()

                                LaunchedEffect(isAcademyOpen)   { if (isAcademyOpen)   { activeTab = "academy";   vm.setAcademyOpen(false) } }
                                LaunchedEffect(isResourcesOpen) { if (isResourcesOpen) { activeTab = "resources"; vm.setResourcesOpen(false) } }

                                // Only redirect to settings if API key is KNOWN to be blank (after data loaded)
                                LaunchedEffect(isDataLoaded, customApiKey) {
                                    if (isDataLoaded && customApiKey.isBlank() && vm.groqApiKey.value.isBlank()) {
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

                                CompositionLocalProvider(LocalLayoutDirection provides if (isArLocal) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
                                        containerColor = VoidBlack,
                                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                        bottomBar = {
                                            AnimatedVisibility(visible = navBarVisible, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                                                AppBottomNavBar(isArabic = isArLocal, activeTab = activeTab) { tab ->
                                                    activeTab = tab; vm.setSettingsOpen(tab == "settings")
                                                }
                                            }
                                        }
                                    ) { innerPadding ->
                                        Box(modifier = Modifier.padding(innerPadding)) {
                                            when (activeTab) {
                                                "home"      -> DashboardScreen(vm, prayerVm, { navController.navigate("link_scanner") }, { t, s, ok -> triggerBiometricAuth(t, s, ok) })
                                                "academy"   -> AcademyScreen(vm, onNavigateToGlossary = { navController.navigate("glossary") }, onNavigateToChecklist = { navController.navigate("checklist") })
                                                "resources" -> ResourcesScreen(vm) { activeTab = "home" }
                                                "library"   -> LibraryScreen(vm) { activeTab = "home" }
                                                "chat"      -> GroqChatScreen(chatVm, uiConfig, isArLocal)
                                                "settings"  -> SettingsScreen(vm, { activeTab = "home" }, { navController.navigate("about") }, { navController.navigate("adaptive_feedback") }) { t, s, ok -> triggerBiometricAuth(t, s, ok) }
                                            }
                                        }
                                    }
                                }
                            }
                            composable("link_scanner")      { LinkScannerScreen(vm) { navController.popBackStack() } }
                            composable("about")             { AboutScreen { navController.popBackStack() } }
                            composable("glossary")          { CyberGlossaryScreen(vm) { navController.popBackStack() } }
                            composable("checklist")         { SecurityChecklistScreen(vm) { navController.popBackStack() } }
                            composable("adaptive_feedback") { val isArLocal by vm.isArabic.collectAsState(); FeedbackScreen(vm, isArLocal) { navController.popBackStack() } }
                        }
                    }
                }
            }
        }
    }
}

private data class NavTab(val key: String, val labelEn: String, val labelAr: String)

@Composable
private fun AppBottomNavBar(isArabic: Boolean, activeTab: String, onTabSelected: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val tabs = listOf(
        NavTab("home", "Home", "الرئيسية"),
        NavTab("academy", "Academy", "الأكاديمية"),
        NavTab("chat", "Chat", "الدردشة"),
        NavTab("library", "Library", "المكتبة"),
        NavTab("settings", "Settings", "الإعدادات")
    )

    val infiniteTransition = rememberInfiniteTransition(label = "nav_glow")
    val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.9f, animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "glow")

    Box(modifier = Modifier.fillMaxWidth()) {
        // Glowing top border line
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, CyberCyan.copy(alpha = glowAlpha * 0.5f), AmberZen.copy(alpha = glowAlpha * 0.3f), CyberCyan.copy(alpha = glowAlpha * 0.5f), Color.Transparent))).align(Alignment.TopCenter))

        NavigationBar(containerColor = Color(0xFF02050D).copy(alpha = 0.98f), tonalElevation = 0.dp, windowInsets = WindowInsets.navigationBars) {
            tabs.forEach { tab ->
                val selected = activeTab == tab.key
                val icon = when (tab.key) {
                    "home"      -> Icons.Rounded.Home
                    "academy"   -> Icons.Rounded.School
                    "chat"      -> Icons.Rounded.SmartToy
                    "resources" -> Icons.Rounded.MenuBook
                    "library"   -> Icons.Rounded.AutoStories
                    else        -> Icons.Rounded.Settings
                }
                val tabColor = when (tab.key) {
                    "home"      -> CyberCyan
                    "academy"   -> Color(0xFF00FF88)
                    "chat"      -> Color(0xFFBB66FF)
                    "resources" -> AmberZen
                    "library"   -> Color(0xFFAA77FF)
                    else        -> Color(0xFFFF6B35)
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onTabSelected(tab.key) },
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            // Glow halo for active tab
                            if (selected) {
                                Box(modifier = Modifier.size(46.dp).background(tabColor.copy(alpha = glowAlpha * 0.12f), RoundedCornerShape(14.dp)).border(1.dp, tabColor.copy(alpha = glowAlpha * 0.5f), RoundedCornerShape(14.dp)))
                            }
                            Icon(icon, tab.labelEn, tint = if (selected) tabColor else Color.White.copy(alpha = 0.25f), modifier = Modifier.size(22.dp))
                        }
                    },
                    label = {
                        Text(if (isArabic) tab.labelAr else tab.labelEn,
                            color = if (selected) tabColor else Color.White.copy(alpha = 0.25f),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    }
}
