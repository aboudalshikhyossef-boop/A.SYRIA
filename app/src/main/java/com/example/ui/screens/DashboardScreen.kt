package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import com.example.data.ContextualVerseEngine
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.VoidBlack
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaQuranFont
import com.example.ui.brand.AyriaColors
import com.example.ui.brand.AyriaGlassPanel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.adaptive.LocalAdaptiveConfig
import com.example.adaptive.LocalWindowAdaptiveInfo
import com.example.adaptive.UIConfig
import kotlin.random.Random
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.text.selection.SelectionContainer




// Space Particle representing deep parallax fields
data class SpaceParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val opacity: Float
)

/**
 * Resolves the AI-configured accent color from UIConfig.
 * Priority: primaryHex > accentColor named token > CyberCyan fallback.
 * Pure function — safe to call directly inside @Composable (triggers recompose via LocalAdaptiveConfig).
 */
private fun resolveAccentFromConfig(config: UIConfig): Color {
    if (config.primaryHex.isNotEmpty()) {
        return try {
            val cleaned = config.primaryHex.trimStart('#')
            val value: Long = when (cleaned.length) {
                6 -> 0xFF000000L or cleaned.toLong(16)
                8 -> cleaned.toLong(16)
                else -> return CyberCyan
            }
            Color(value.toInt())
        } catch (_: Exception) { CyberCyan }
    }
    return when (config.accentColor) {
        "amber"  -> AmberZen
        "green"  -> Color(0xFF00FF88)
        "white"  -> Color(0xFFE0E0E0)
        "red"    -> Color(0xFFFF3D5A)
        "purple" -> Color(0xFFBB86FC)
        else     -> CyberCyan
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    prayerViewModel: com.example.ui.viewmodel.PrayerViewModel,
    onNavigateToScanner: () -> Unit,
    onVaultLockRequest: (String, String, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val windowInfo = LocalWindowAdaptiveInfo.current

    val roll by viewModel.roll.collectAsState()
    val pitch by viewModel.pitch.collectAsState()

    // Phase 7 Settings & Stealth Mode state tracking
    val isStealthMode by viewModel.isStealthMode.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val stealthAlphaMultiplier = if (isStealthMode) 0.45f else 1f

    // Phase 6 Ambient Wisdom state tracking
    val ambientInsight by viewModel.ambientInsight.collectAsState()
    val stateType = ambientInsight.stateType

    // Telemetry and analytical data
    val connectionType by viewModel.connectionType.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    
    // Accurate Prayer Data from PrayerViewModel
    val nextPrayerName by prayerViewModel.nextPrayerName.collectAsState()
    val nextPrayerCountdown by prayerViewModel.nextPrayerCountdown.collectAsState()
    val nextPrayerProgress by prayerViewModel.nextPrayerProgress.collectAsState()
    val nextPrayerTime by prayerViewModel.nextPrayerTime.collectAsState()
    val allPrayerTimes by prayerViewModel.allPrayerTimes.collectAsState()
    val qiblaDirection by prayerViewModel.qiblaDirection.collectAsState()

    val azimuth by viewModel.azimuth.collectAsState()

    // Pulse animation for HUD components
    val infiniteTransition = rememberInfiniteTransition(label = "hudPulse")
    val hudPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hudPulseAlpha"
    )

    fun openIntelligenceLink(url: String, title: String) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        viewModel.analyzeResourceLink(url)
    }

    // Gemini states
    val isThinking by viewModel.isThinking.collectAsState()
    val intelligenceBrief by viewModel.intelligenceBrief.collectAsState()
    val terminalInput by viewModel.terminalInput.collectAsState()
    val terminalResponse by viewModel.terminalResponse.collectAsState()
    val isTerminalExpanded by viewModel.isTerminalExpanded.collectAsState()

    // Secure vault state
    val isVaultViewOpen by viewModel.isVaultViewOpen.collectAsState()

    // Phase 11 states
    val isAr by viewModel.isArabic.collectAsState()
    val cyberScore by viewModel.cyberScore.collectAsState()
    val cyberRank by viewModel.cyberRank.collectAsState()
    val cyberProgress by viewModel.cyberProgress.collectAsState()
    val isAcademyOpen by viewModel.isAcademyOpen.collectAsState()
    val isResourcesOpen by viewModel.isResourcesOpen.collectAsState()

    val operatorName by viewModel.operatorName.collectAsState()
    val neuralRole by viewModel.neuralRole.collectAsState()

    val isNeuralLinkOffline by viewModel.isNeuralLinkOffline.collectAsState()
    val linkAnalysisResult by viewModel.linkAnalysisResult.collectAsState()
    val isAnalyzingLink by viewModel.isAnalyzingLink.collectAsState()

    // Phase 20: Tactical Document Details

    // Desktop Mode state
    var isDesktopModeOpen by remember { mutableStateOf(false) }

    // Navigation BackHandler logic for sovereign stability
    BackHandler(enabled = isDesktopModeOpen || isTerminalExpanded || isVaultViewOpen) {
        when {
            isDesktopModeOpen -> isDesktopModeOpen = false
            isTerminalExpanded -> viewModel.setTerminalExpanded(false)
            isVaultViewOpen -> viewModel.setVaultViewOpen(false)
        }
    }

    // Cubic bezier easing for organic visual pulse cycle
    val cubicBezierEasing = remember { CubicBezierEasing(0.445f, 0.05f, 0.55f, 0.95f) }

    val smoothRoll by androidx.compose.animation.core.animateFloatAsState(
        targetValue = roll,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
        ),
        label = "smoothRoll"
    )

    val smoothPitch by androidx.compose.animation.core.animateFloatAsState(
        targetValue = pitch,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
        ),
        label = "smoothPitch"
    )

    // Multi-layered Parallax dimensions derived from smooth accelerometer roll and pitch
    val bgRollShift by remember { derivedStateOf { smoothRoll * 2.8f } }
    val bgPitchShift by remember { derivedStateOf { smoothPitch * 2.8f } }

    val coreRollShift by remember { derivedStateOf { smoothRoll * 1.5f } }
    val corePitchShift by remember { derivedStateOf { smoothPitch * 1.5f } }

    val cardRollShift by remember { derivedStateOf { smoothRoll * 0.8f } }
    val cardPitchShift by remember { derivedStateOf { smoothPitch * 0.8f } }

        val spiritualVerses = remember(isAr) {
        if (isAr) {
            listOf(
                "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                "وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
                "يَهْدِي اللَّهُ لِنُورِهِ مَنْ يَشَاءُ",
                "وَكَانَ حقًّا عَلَيْنَا نَصْرُ الْمُؤْمِنِينَ"
            )
        } else {
            listOf(
                "Verily, in the remembrance of Allah do hearts find rest",
                "Indeed, with hardship comes ease",
                "And whoever relies upon Allah - then He is sufficient for him",
                "Allah guides to His light whom He wills",
                "And it was due from Us to aid the believers"
            )
        }
    }
    var currentVerseIndex by remember { mutableStateOf(0) }

    val bgColors = when {
        isStealthMode -> listOf(Color(0xFF020711), Color(0xFF030A14))
        isNeuralLinkOffline -> listOf(Color(0xFF101624), Color(0xFF030A14))
        else -> listOf(Color(0xFF0A263C), Color(0xFF020915), Color(0xFF061426))
    }

    // ── AI-driven theme accent — reads live from LocalAdaptiveConfig so every color
    //    change issued by the AI Brain instantly re-triggers Compose recomposition
    //    across the entire DashboardScreen without any app restart.
    val adaptiveConfig = LocalAdaptiveConfig.current
    val themeAccent = resolveAccentFromConfig(adaptiveConfig)

    // Connection-aware dynamic accent: AI-theme when ONLINE, Orange when OFFLINE
    val dynamicAccent by animateColorAsState(
        targetValue = if (isNeuralLinkOffline) AmberZen else themeAccent,
        animationSpec = tween(800),
        label = "dynamicAccent"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = bgColors,
                    radius = 2200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Deep Parallax with Stealth mode speed deceleration support
        IceTriangleBackdrop(
            rollOffset = bgRollShift,
            pitchOffset = bgPitchShift,
            isStealthMode = isStealthMode
        )

        BackgroundParticles(
            rollOffset = bgRollShift,
            pitchOffset = bgPitchShift,
            stateType = stateType,
            isStealthMode = isStealthMode
        )

        // Subtle gradient vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Conditional Rendering: Main Dashboard or Strategic Terminal Mode
        val currentLayoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
            if (!isTerminalExpanded) {
                // STANDARD MODE: Unified Operations Grid with LazyColumn fix for scrolling
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        
                        .padding(horizontal = windowInfo.horizontalContentPadding, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatusHeaderCell(
                            roll = cardRollShift,
                            pitch = cardPitchShift,
                            operatorName = operatorName,
                            neuralRole = neuralRole,
                            isStealth = isStealthMode
                        )
                    }

                    // Connection-aware status pill (blue when online, orange when offline)
                    item {
                        ConnectionStatusPill(
                            isOnline = !isNeuralLinkOffline,
                            accent = dynamicAccent,
                            isAr = isAr
                        )
                    }
                    
                    item {
                        SovereignContextHub(
                            roll = cardRollShift,
                            pitch = cardPitchShift,
                            connectionType = connectionType,
                            ipAddress = ipAddress
                        )
                    }

                    // Phase 6 Contextual Wisdom floating card
                    item {
                        AmbientInsightCard(
                            insight = ambientInsight,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Phase 11: Glowing Cyber-Rank tracker bar
                    item {
                        CyberRankMetricBar(
                            score = cyberScore,
                            rank = cyberRank,
                            progress = cyberProgress,
                            roll = cardRollShift,
                            pitch = cardPitchShift
                        )
                    }

                    item {
                        Text(
                            text = if (isAr) "اضغط مطولاً على درع النقاط لتنشغيل العصف الاستراتيجي" else "HOLD SHIELD CORE TO ACTIVATE STRATEGIC BRAIN",
                            color = CyberCyan.copy(alpha = 0.5f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Core responsive node that transitions visually during thinking operations
                    item {
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.setTerminalExpanded(true)
                                        }
                                    )
                                }
                        ) {
                            NeuralCore(
                                roll = coreRollShift,
                                pitch = corePitchShift,
                                isThinking = isThinking,
                                easing = cubicBezierEasing,
                                sizeDimension = 190,
                                stateType = stateType
                            )
                        }
                    }

                    // Tactical Holy Verse Selection Bridge
                    item {
                        NeuralVerseModule(
                            verse = spiritualVerses[currentVerseIndex],
                            easing = cubicBezierEasing,
                            onTap = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                currentVerseIndex = (currentVerseIndex + 1) % spiritualVerses.size
                            }
                        )
                    }

                    // Secure vault entry
                    item {
                        TacticalGridButton(
                            text = if (isAr) "الخزينة الآمنة" else "SECURE VAULT",
                            subtitle = if (isAr) "حفظ الصور والملفات بتشفير محلي" else "Encrypted photos, files and notes",
                            color = dynamicAccent,
                            icon = Icons.Rounded.Lock,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                if (viewModel.isVaultAuthenticated.value) {
                                    viewModel.setVaultViewOpen(true)
                                } else {
                                    onVaultLockRequest(
                                        if (isAr) "تأكيد الهوية السيادية" else "SOVEREIGN IDENTITY VERIFIED",
                                        if (isAr) "مطلوب التحقق بالبصمة أو قفل الجهاز للوصول إلى الخزينة" else "Biometric or secure device authentication is required",
                                        {
                                            viewModel.setVaultAuthenticated(true)
                                            viewModel.setVaultViewOpen(true)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        )
                    }

                    // Academy and resource controllers
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .graphicsLayer {
                                    rotationX = -cardPitchShift * 0.7f
                                    rotationY = cardRollShift * 0.7f
                                },
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TacticalGridButton(
                                text = if (isAr) "الأكاديمية" else "ACADEMY",
                                subtitle = if (isAr) "تعلم واختبارات تفاعلية" else "Interactive learning",
                                color = dynamicAccent,
                                icon = Icons.Rounded.School,
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.setAcademyOpen(true)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            TacticalGridButton(
                                text = if (isAr) "المصادر" else "RESOURCES",
                                subtitle = if (isAr) "أدوات ومعرفة مختارة" else "Curated tools and knowledge",
                                color = dynamicAccent,
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.setResourcesOpen(true)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        IntelligenceTicker(
                            text = intelligenceBrief,
                            isOffline = isNeuralLinkOffline,
                            roll = cardRollShift,
                            pitch = cardPitchShift
                        )
                    }

                    item {
                        // Desktop Mode Button
                        Button(
                            onClick = { isDesktopModeOpen = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(0.75.dp, CyberCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isAr) "وضع المكتب" else "DESKTOP MODE",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        RadiantDigitalClock(
                            prayerName = nextPrayerName,
                            prayerTime = nextPrayerTime,
                            prayerCountdown = nextPrayerCountdown,
                            isAr = isAr,
                            allPrayerTimes = allPrayerTimes
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
            // TERMINAL EXTENDED MODE: Redesigned Strategic Station with Scrollable Area and Blinking Cursor
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    
                    .imePadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.setTerminalExpanded(false) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = CyberCyan)
                    }
                    val statusColor by animateColorAsState(if (isNeuralLinkOffline) Color.Red else CyberCyan, label = "statusColor")
                    Text(
                        text = if (isNeuralLinkOffline) "LINK_OFFLINE" else "UPLINK_STABLE",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Output Panel (Neural Console)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    val terminalState = rememberScrollState()
                    LaunchedEffect(terminalResponse) {
                        terminalState.animateScrollTo(terminalState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(terminalState)
                    ) {
                        Text(
                            text = if (isAr) "--- بداية التقرير الاستراتيجي ---" else "--- BEGIN STRATEGIC REPORT ---",
                            color = CyberCyan.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        SelectionContainer {
                            CompositionLocalProvider(LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr)) {
                                val cursorColor by animateColorAsState(if (isThinking) AmberZen else CyberCyan, label = "cursor")
                                val infiniteCursor = rememberInfiniteTransition(label = "cursor")
                                val cursorAlpha by infiniteCursor.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                    label = "cursorAlpha"
                                )

                                Text(
                                    text = if (terminalResponse.isEmpty() && !isThinking) {
                                        if (isAr) "محطة التشفير جاهزة.\nبناء وتطوير: ABOUDA.AL.SHEKH.YOSSEF\nبانتظار استعلام الهوية..." else "TERMINAL READY.\nEngineered by: ABOUDA.AL.SHEKH.YOSSEF\nAWAITING CORE QUERY..."
                                    } else {
                                        terminalResponse + (if (isThinking || terminalResponse.isNotEmpty()) " " else "")
                                    },
                                    color = if (isThinking) AmberZen else CyberCyan,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 22.sp
                                )

                                if (isThinking || terminalResponse.isNotEmpty()) {
                                    Text(
                                        text = "_",
                                        color = cursorColor.copy(alpha = cursorAlpha),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.offset(x = if (isAr) (-2).dp else 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Read-only report mode — AI status dashboard only
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(0.5.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isAr)
                            "[ تقرير الحالة ] — واجهة للقراءة فقط. استخدم الأكاديمية لاستعلامات الذكاء الاصطناعي."
                        else
                            "[ STATUS REPORT ] — Read-only view. Use Academy for AI interactions.",
                        color = CyberCyan.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ==========================================
        // PHASE 19: NEURAL LINK ANALYSIS OVERLAY
        // ==========================================
        AnimatedVisibility(
            visible = isAnalyzingLink || linkAnalysisResult != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { viewModel.clearLinkAnalysis() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(VoidBlack, RoundedCornerShape(16.dp))
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEURAL LINK ANALYSIS",
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (isAnalyzingLink) {
                        NeuralCore(
                            roll = 0f, pitch = 0f, 
                            isThinking = true, easing = LinearEasing,
                            sizeDimension = 80, stateType = ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isAr) "تحليل الرابط رقمياً..." else "DECODING BRAIN LINK...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (linkAnalysisResult != null) {
                        Text(
                            text = linkAnalysisResult!!,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.clearLinkAnalysis() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLOSE", fontWeight = FontWeight.Bold, color = VoidBlack)
                        }
                    }
                }
            }
        }
    }

        if (isVaultViewOpen) {
            SecureVaultScreen(
                viewModel = viewModel,
                isArabic = isAr,
                onClose = { viewModel.setVaultViewOpen(false) }
            )
        }

        // Floating settings control in matching glass material.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 18.dp)
                .size(44.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0x553C8CAB), Color(0x33132D48))),
                    RoundedCornerShape(16.dp)
                )
                .border(0.7.dp, Color.White.copy(alpha = 0.19f), RoundedCornerShape(16.dp))
                .clickable {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.setSettingsOpen(true)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = if (isAr) "الإعدادات" else "Settings",
                tint = AyriaColors.glacier,
                modifier = Modifier.size(21.dp)
            )
        }

        // Overlay Screen when isSettingsOpen is true
        if (isSettingsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .pointerInput(Unit) {}, // Consume taps
                contentAlignment = Alignment.Center
            ) {
                var keyInput by remember { mutableStateOf(customApiKey) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .background(VoidBlack.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isAr) "وحدة الإعدادات" else "SOVEREIGN CONFIGURATION UNIT",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr) "تحكم متقدم في إعدادات التطبيق" else "A.SYRIA V4 ADVANCED USER CONTROLS",
                        color = CyberCyan.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gemini API Key input field
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        label = { Text("GEMINI API KEY OVERRIDE", color = CyberCyan.copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("Enter custom key...", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCyan.copy(alpha = 0.3f),
                            cursorColor = CyberCyan
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stealth Mode Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .background(GlassWhite, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                viewModel.setStealthMode(!isStealthMode)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "وضع التخفي" else "STEALTH MODE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isAr) "يخفف الإضاءة ويبطئ الحركات" else "Dims global UI & slows celestial drift",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(22.dp)
                                .border(1.dp, if (isStealthMode) CyberCyan else Color.White.copy(alpha = 0.3f), RoundedCornerShape(11.dp))
                                .background(if (isStealthMode) CyberCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(11.dp))
                                .padding(2.dp),
                            contentAlignment = if (isStealthMode) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(if (isStealthMode) CyberCyan else Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dialog Actions (Save & Close)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                viewModel.setSettingsOpen(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CLOSE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.updateCustomApiKey(keyInput)
                                viewModel.setSettingsOpen(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                            border = BorderStroke(0.75.dp, CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SAVE CONFIG", color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

            }
        }

        // Desktop Mode Overlay
        if (isDesktopModeOpen) {
            DesktopModeScreen(
                prayerName = nextPrayerName,
                prayerTime = nextPrayerTime,
                prayerCountdown = nextPrayerCountdown,
                allPrayerTimes = allPrayerTimes,
                isAr = isAr,
                isOnline = !isNeuralLinkOffline,
                onClose = { isDesktopModeOpen = false }
            )
        }
    }
}

// ==========================================
// COMPOSE SUB-COMPONENTS & LAYOUTS
// ==========================================

// Phase 6 Contextual Wisdom Ambient Insight Typewriter Board
@Composable
private fun IceTriangleBackdrop(
    rollOffset: Float,
    pitchOffset: Float,
    isStealthMode: Boolean
) {
    val transition = rememberInfiniteTransition(label = "ice_prism_drift")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "ice_prism_shimmer"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(
            x = size.width * 0.51f + rollOffset * 0.7f,
            y = size.height * 0.39f + pitchOffset * 0.7f
        )
        val radius = size.minDimension * 0.56f
        val top = Offset(center.x, center.y - radius * 0.98f)
        val left = Offset(center.x - radius * 0.84f, center.y + radius * 0.52f)
        val right = Offset(center.x + radius * 0.84f, center.y + radius * 0.52f)
        val base = Offset(center.x, center.y + radius * 0.52f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x553BC4E8).copy(alpha = if (isStealthMode) 0.18f else 0.42f),
                    Color(0x1C2C78B7),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.32f
            ),
            radius = radius * 1.32f,
            center = center
        )
        val prism = androidx.compose.ui.graphics.Path().apply {
            moveTo(top.x, top.y)
            lineTo(left.x, left.y)
            lineTo(right.x, right.y)
            close()
        }
        drawPath(
            path = prism,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x2B9FEAFF), Color(0x123C91C9), Color(0x228CEBFF)),
                start = top,
                end = right
            ),
            alpha = if (isStealthMode) 0.32f else 0.72f
        )
        drawPath(
            path = prism,
            color = Color(0x8ADBF8FF).copy(alpha = if (isStealthMode) 0.035f else 0.085f + shimmer * 0.025f),
            style = Stroke(width = 1.1.dp.toPx())
        )
        drawLine(color = Color.White.copy(alpha = 0.045f + shimmer * 0.025f), start = top, end = base, strokeWidth = 1.dp.toPx())
        drawLine(color = Color(0xFF8FE6FF).copy(alpha = 0.06f), start = left, end = Offset(center.x, center.y + radius * 0.03f), strokeWidth = 0.8.dp.toPx())
        drawLine(color = Color(0xFF8FE6FF).copy(alpha = 0.06f), start = right, end = Offset(center.x, center.y + radius * 0.03f), strokeWidth = 0.8.dp.toPx())
    }
}

@Composable
fun AmbientInsightCard(
    insight: ContextualVerseEngine.SpiritualInsight,
    modifier: Modifier = Modifier
) {
    val fullText = remember(insight) {
        "${insight.systemAnalysis}. ${insight.verseArabic} (${insight.verseReference}) — ${insight.verseTranslation}"
    }
    var typedText by remember(insight) { mutableStateOf("") }
    LaunchedEffect(fullText) {
        typedText = ""
        for (index in 1..fullText.length) {
            typedText = fullText.substring(0, index)
            kotlinx.coroutines.delay(15)
        }
    }
    val stateColor = when (insight.stateType) {
        ContextualVerseEngine.AmbientStateType.CRITICAL -> Color(0xFFFFC68C)
        ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> AyriaColors.teal
        ContextualVerseEngine.AmbientStateType.NORMAL -> AyriaColors.glacier
    }
    AyriaGlassPanel(
        modifier = modifier.fillMaxWidth(),
        highlighted = insight.stateType != ContextualVerseEngine.AmbientStateType.NORMAL,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (insight.stateType == ContextualVerseEngine.AmbientStateType.CRITICAL) "تنبيه" else "ومضة معرفية",
                    color = stateColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AyriaArabicFont
                )
                Box(Modifier.size(6.dp).background(stateColor.copy(alpha = 0.88f), RoundedCornerShape(50)))
            }
            Spacer(Modifier.height(7.dp))
            Text(
                text = typedText,
                color = AyriaColors.pearl.copy(alpha = 0.94f),
                fontSize = 11.sp,
                fontFamily = AyriaArabicFont,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

// Responsive liquid-glass action card shared by the vault, academy and resources.
@Composable
fun TacticalGridButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    icon: ImageVector = Icons.Rounded.Lock
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .heightIn(min = 88.dp)
            .background(
                Brush.linearGradient(listOf(Color(0x293B91B6), Color(0x1B173B59), Color(0x16FFFFFF))),
                shape
            )
            .border(
                0.7.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.21f), color.copy(alpha = 0.24f), Color.White.copy(alpha = 0.07f))),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(color.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AyriaColors.glacier, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    color = AyriaColors.pearl,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AyriaArabicFont,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = AyriaColors.muted.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontFamily = AyriaArabicFont,
                        textAlign = TextAlign.Start,
                        lineHeight = 13.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundParticles(
    rollOffset: Float, 
    pitchOffset: Float,
    stateType: ContextualVerseEngine.AmbientStateType = ContextualVerseEngine.AmbientStateType.NORMAL,
    isStealthMode: Boolean = false
) {
    val durationMs = when {
        isStealthMode -> 32000 // Incredibly slow speed drift representing deep stealth
        stateType == ContextualVerseEngine.AmbientStateType.CRITICAL -> 15000 // Slow speed drift
        stateType == ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> 4500 // Quick active stream
        else -> 8000 // Normal drift
    }
    
    val baseColor = when {
        isStealthMode -> Color.Gray.copy(alpha = 0.35f)
        stateType == ContextualVerseEngine.AmbientStateType.CRITICAL -> AmberZen
        stateType == ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> CyberCyan
        else -> CyberCyan
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles_movement")
    val autoOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auto_offset"
    )

    val particles = remember {
        List(48) {
            SpaceParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3.5f + 1f,
                opacity = Random.nextFloat() * 0.55f + 0.15f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Pre-calculate all particle positions for connection lines
        val positions = particles.map { p ->
            val driftFactor = if (stateType == ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE) {
                autoOffset * (p.size * 0.6f + 0.4f)
            } else {
                autoOffset * 0.25f
            }

            var px = (p.x * width + rollOffset * 1.5f) % width
            var py = (p.y * height + pitchOffset * 1.5f + driftFactor) % height

            if (px < 0) px += width
            if (py < 0) py += height

            Offset(px, py) to p
        }

        // Draw particles
        positions.forEach { (offset, p) ->
            drawCircle(
                color = baseColor.copy(alpha = p.opacity),
                radius = p.size,
                center = offset
            )
        }

        // Draw neural network connection lines between nearby particles
        for (i in positions.indices) {
            for (j in i + 1 until positions.size) {
                val (posA, pA) = positions[i]
                val (posB, pB) = positions[j]
                val dx = posA.x - posB.x
                val dy = posA.y - posB.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                if (distance < 120f) {
                    val lineAlpha = ((1f - distance / 120f) * 0.15f) * ((pA.opacity + pB.opacity) / 2f)
                    drawLine(
                        color = baseColor.copy(alpha = lineAlpha),
                        start = posA,
                        end = posB,
                        strokeWidth = 0.5f
                    )
                }
            }
        }
    }
}

@Composable
fun SovereignContextHub(
    roll: Float,
    pitch: Float,
    connectionType: String,
    ipAddress: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = -pitch * 0.22f
                rotationY = roll * 0.22f
                translationX = roll * 0.35f
                translationY = pitch * 0.35f
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AyriaGlassPanel(
            modifier = Modifier.weight(1f).heightIn(min = 92.dp),
            highlighted = true,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(34.dp).background(Color(0x3322C7E8), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Wifi, contentDescription = null, tint = AyriaColors.glacier, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text("اتصال الشبكة", color = AyriaColors.glacier, fontFamily = AyriaArabicFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(connectionType.ifBlank { "غير متاح" }, color = AyriaColors.pearl, fontFamily = AyriaArabicFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ipAddress.ifBlank { "—" },
                    color = AyriaColors.muted.copy(alpha = 0.82f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        AyriaGlassPanel(
            modifier = Modifier.weight(1f).heightIn(min = 92.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(34.dp).background(Color(0x332A8FC9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Explore, contentDescription = null, tint = AyriaColors.glacier, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text("استجابة الجيروسكوب", color = AyriaColors.glacier, fontFamily = AyriaArabicFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("حركة متزامنة", color = AyriaColors.pearl.copy(alpha = 0.9f), fontFamily = AyriaArabicFont, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "X ${String.format(java.util.Locale.US, "%.1f", roll)}",
                        color = AyriaColors.muted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Y ${String.format(java.util.Locale.US, "%.1f", pitch)}",
                        color = AyriaColors.muted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun NeuralCore(
    roll: Float,
    pitch: Float,
    isThinking: Boolean,
    easing: androidx.compose.animation.core.Easing,
    sizeDimension: Int,
    stateType: ContextualVerseEngine.AmbientStateType = ContextualVerseEngine.AmbientStateType.NORMAL
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuralLoop")

    val outerRingDuration = when (stateType) {
        ContextualVerseEngine.AmbientStateType.CRITICAL -> 24000
        ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> 6000
        else -> 12000
    }

    val middleRingDuration = when (stateType) {
        ContextualVerseEngine.AmbientStateType.CRITICAL -> 17000
        ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> 4500
        else -> 8500
    }

    // Slow outward rot rotation
    val outerRingRot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(outerRingDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRing"
    )

    // Reverse quick middle rot rotation
    val middleRingRot by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(middleRingDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MiddleRing"
    )

    // Double pulse speed when "thinking" or high performance active
    val pulseDurationMillis = when {
        isThinking -> 1100
        stateType == ContextualVerseEngine.AmbientStateType.HIGH_PERFORMANCE -> 1600
        stateType == ContextualVerseEngine.AmbientStateType.CRITICAL -> 4500
        else -> 2600
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMillis, easing = easing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Core visual shifts based on state and Gemini calculations
    val primaryCoreColor = when {
        stateType == ContextualVerseEngine.AmbientStateType.CRITICAL -> Color(0xFFFFC68C)
        isThinking -> Color(0xFFB7F4FF)
        else -> Color(0xFF8EDDF7)
    }
    val secondaryCoreColor = when {
        stateType == ContextualVerseEngine.AmbientStateType.CRITICAL -> Color(0xFFE99A52)
        isThinking -> Color(0xFF73D9E9)
        else -> Color(0xFF9ABFFF)
    }

    Box(
        modifier = Modifier
            .size(sizeDimension.dp)
            .graphicsLayer {
                rotationX = -pitch * 1.2f
                rotationY = roll * 1.2f
                translationX = roll * 1.1f
                translationY = pitch * 1.1f
                scaleX = pulseScale
                scaleY = pulseScale
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f

            // Outer Orbit: Alternating Dashed Cyber Ring
            rotate(outerRingRot, pivot = center) {
                drawCircle(
                    color = primaryCoreColor.copy(alpha = 0.35f),
                    radius = r * 0.86f,
                    style = Stroke(
                        width = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                    )
                )
            }

            // Middle Orbit: Golden Barrier Shield Ring
            rotate(middleRingRot, pivot = center) {
                drawCircle(
                    color = secondaryCoreColor.copy(alpha = 0.55f),
                    radius = r * 0.68f,
                    style = Stroke(
                        width = 4.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(65f, 25f), 0f)
                    )
                )
            }

            // Inner Core Structure
            drawCircle(
                color = primaryCoreColor.copy(alpha = 0.12f),
                radius = r * 0.48f
            )
            drawCircle(
                color = primaryCoreColor.copy(alpha = 0.8f),
                radius = r * 0.46f,
                style = Stroke(width = 2.5f)
            )

            // Absolute Inner Satellite Node
            drawCircle(
                color = secondaryCoreColor,
                radius = r * 0.15f
            )
            drawCircle(
                color = Color.White,
                radius = r * 0.05f
            )
        }
    }
}

@Composable
fun NeuralVerseModule(verse: String, easing: CubicBezierEasing, onTap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "VerseGlow")
    
    val opacityGlow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = easing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OpacityGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Beautiful quranic verse text with AmberZen drop shadow glow
        Text(
            text = verse,
            color = AyriaColors.glacier,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = AyriaColors.teal.copy(alpha = 0.48f),
                    offset = Offset(0f, 0f),
                    blurRadius = 15f
                )
            ),
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaQuranFont,
            lineHeight = 31.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer { alpha = opacityGlow }
                .padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "اضغط لتغيير الآية",
            color = AyriaColors.muted.copy(alpha = 0.74f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = AyriaArabicFont,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QiblaCompass(
    azimuth: Float,
    qiblaAngle: Double,
    roll: Float,
    pitch: Float,
    pulseAlpha: Float
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Normalize difference to find alignment
    val diff = kotlin.math.abs(azimuth - qiblaAngle.toFloat())
    val isAligned = diff < 6 || diff > 354
    
    val ringColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isAligned) AmberZen else CyberCyan,
        animationSpec = tween(400),
        label = "ringColor"
    )

    val shadowColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isAligned) AmberZen.copy(alpha = 0.6f) else CyberCyan.copy(alpha = 0.6f),
        animationSpec = tween(400),
        label = "shadowColor"
    )

    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = Modifier
            .size(180.dp)
            .graphicsLayer {
                rotationX = -pitch * 0.6f
                rotationY = roll * 0.6f
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f
            
            // Draw the 3D-looking glowing ring
            drawCircle(
                color = ringColor.copy(alpha = 0.15f * pulseAlpha),
                radius = radius,
                style = Stroke(width = 12f)
            )
            drawCircle(
                color = ringColor.copy(alpha = 0.4f * pulseAlpha),
                radius = radius,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            
            // Rotate the entire inner compass based on device heading
            rotate(-azimuth, pivot = center) {
                // North Marker
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y - radius + 20f),
                    strokeWidth = 4f
                )
                
                // Qibla Marker (The target)
                rotate(qiblaAngle.toFloat(), pivot = center) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x, center.y - radius - 15f)
                        lineTo(center.x - 12f, center.y - radius + 15f)
                        lineTo(center.x + 12f, center.y - radius + 15f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = ringColor
                    )
                    
                    // Core Directional Line
                    drawLine(
                        color = ringColor.copy(alpha = 0.4f),
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                }
            }
        }
        
        // Center Hub
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(VoidBlack, RoundedCornerShape(100))
                .border(1.dp, ringColor.copy(alpha = 0.5f), RoundedCornerShape(100)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isAligned) "⚡" else "◈",
                color = ringColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Prayer name mapping to Arabic
private fun prayerNameToArabic(name: String): String = when (name.uppercase()) {
    "FAJR" -> "الفجر"
    "SUNRISE" -> "الشروق"
    "DHUHR" -> "الظهر"
    "ASR" -> "العصر"
    "MAGHRIB" -> "المغرب"
    "ISHA" -> "العشاء"
    "NONE" -> "الفجر"
    else -> name
}

@Composable
fun RadiantDigitalClock(
    prayerName: String,
    prayerTime: String = "00:00",
    prayerCountdown: String = "00:00:00",
    isAr: Boolean,
    allPrayerTimes: Map<String, String> = emptyMap()
) {
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.ENGLISH)
        while (true) {
            currentTime = sdf.format(java.util.Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "prayerPulse")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val amberGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "amberGlow"
    )

    val arabicPrayerName = prayerNameToArabic(prayerName)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF010D1A), Color(0xFF000507))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(CyberCyan.copy(alpha = 0.1f), CyberCyan.copy(alpha = 0.4f * glow), CyberCyan.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Scanlines
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 8.dp.toPx()
            var y = 0f
            while (y < size.height) {
                drawLine(CyberCyan.copy(alpha = 0.03f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
                y += spacing
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CyberCyan.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "[ SAT_CLOCK ]",
                        color = CyberCyan.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(AmberZen.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, AmberZen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PRAYER_SYNC",
                        color = AmberZen.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Analog Clock
            AnalogClock(size = 120)

            Spacer(modifier = Modifier.height(12.dp))

            // Main digital clock
            Text(
                text = currentTime,
                color = CyberCyan,
                fontSize = 36.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = CyberCyan.copy(alpha = 0.7f * glow),
                        blurRadius = 30f
                    )
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(0.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, AmberZen.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Prayer section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next prayer name - Arabic
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isAr) "الصلاة القادمة" else "NEXT PRAYER",
                        color = CyberCyan.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = arabicPrayerName,
                        color = AmberZen.copy(alpha = amberGlow),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Light,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = AmberZen.copy(alpha = 0.5f),
                                blurRadius = 15f
                            )
                        )
                    )
                    if (!isAr) {
                        Text(
                            text = prayerName.uppercase(),
                            color = CyberCyan.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Vertical divider
                Box(modifier = Modifier.width(0.5.dp).height(56.dp).background(CyberCyan.copy(alpha = 0.2f)))

                // Prayer time and countdown
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isAr) "الوقت" else "TIME",
                        color = CyberCyan.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prayerTime,
                        color = CyberCyan.copy(alpha = glow),
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = CyberCyan.copy(alpha = 0.6f),
                                blurRadius = 12f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(4.dp).background(AmberZen.copy(alpha = amberGlow), RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = prayerCountdown,
                            color = AmberZen.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = if (isAr) "متبقي" else "REMAINING",
                        color = CyberCyan.copy(alpha = 0.4f),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (allPrayerTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                // All Prayer Times Grid
                val prayerList = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        prayerList.take(3).forEach { name ->
                            PrayerItem(name, allPrayerTimes[name] ?: "--:--", isAr, name == arabicPrayerName)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Row 2
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        prayerList.drop(3).forEach { name ->
                            PrayerItem(name, allPrayerTimes[name] ?: "--:--", isAr, name == arabicPrayerName)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ==========================================
// ANALOG CLOCK COMPOSABLE
// ==========================================
@Composable
fun AnalogClock(size: Int = 200) {
    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var second by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR)
            minute = cal.get(Calendar.MINUTE)
            second = cal.get(Calendar.SECOND)
            kotlinx.coroutines.delay(1000)
        }
    }

    val sizeDp = size.dp
    val infiniteTransition = rememberInfiniteTransition(label = "clockGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "clockGlowAlpha"
    )

    Canvas(modifier = Modifier.size(sizeDp)) {
        val centerX = this.size.width / 2f
        val centerY = this.size.height / 2f
        val radius = minOf(centerX, centerY) * 0.88f

        // Outer glow ring
        drawCircle(
            color = CyberCyan.copy(alpha = 0.08f * glowAlpha),
            radius = radius * 1.08f,
            center = Offset(centerX, centerY)
        )

        // Clock face border
        drawCircle(
            color = CyberCyan.copy(alpha = 0.35f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Inner subtle fill
        drawCircle(
            color = CyberCyan.copy(alpha = 0.06f),
            radius = radius * 0.95f,
            center = Offset(centerX, centerY)
        )

        // Tick marks (60 minute ticks, 12 hour ticks)
        for (i in 0 until 60) {
            val angle = (i * 6f) - 90f
            val isHourMark = i % 5 == 0
            val tickStart = if (isHourMark) radius * 0.78f else radius * 0.88f
            val tickEnd = radius * 0.95f
            val tickColor = if (isHourMark) CyberCyan.copy(alpha = 0.7f) else CyberCyan.copy(alpha = 0.2f)
            val tickWidth = if (isHourMark) 2.5f else 1f
            val rad = angle * PI.toFloat() / 180f
            drawLine(
                color = tickColor,
                start = Offset(centerX + cos(rad) * tickStart, centerY + sin(rad) * tickStart),
                end = Offset(centerX + cos(rad) * tickEnd, centerY + sin(rad) * tickEnd),
                strokeWidth = tickWidth,
                cap = StrokeCap.Round
            )
        }

        // Hour hand (short, thick)
        val hourAngle = ((hour % 12) * 30f + minute * 0.5f) - 90f
        val hourRad = hourAngle * PI.toFloat() / 180f
        drawLine(
            color = CyberCyan.copy(alpha = 0.9f),
            start = Offset(centerX, centerY),
            end = Offset(centerX + cos(hourRad) * radius * 0.5f, centerY + sin(hourRad) * radius * 0.5f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Minute hand (longer, medium)
        val minuteAngle = (minute * 6f + second * 0.1f) - 90f
        val minuteRad = minuteAngle * PI.toFloat() / 180f
        drawLine(
            color = CyberCyan.copy(alpha = 0.8f),
            start = Offset(centerX, centerY),
            end = Offset(centerX + cos(minuteRad) * radius * 0.7f, centerY + sin(minuteRad) * radius * 0.7f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Second hand (longest, thinnest, AmberZen)
        val secondAngle = (second * 6f) - 90f
        val secondRad = secondAngle * PI.toFloat() / 180f
        drawLine(
            color = AmberZen.copy(alpha = 0.9f),
            start = Offset(centerX - cos(secondRad) * radius * 0.15f, centerY - sin(secondRad) * radius * 0.15f),
            end = Offset(centerX + cos(secondRad) * radius * 0.82f, centerY + sin(secondRad) * radius * 0.82f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(color = AmberZen, radius = 3.5f, center = Offset(centerX, centerY))
        drawCircle(
            color = CyberCyan.copy(alpha = 0.5f),
            radius = 6f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1f)
        )
    }
}

// ==========================================
// DESKTOP MODE SCREEN COMPOSABLE
// ==========================================
@Composable
fun DesktopModeScreen(
    prayerName: String,
    prayerTime: String,
    prayerCountdown: String,
    allPrayerTimes: Map<String, String>,
    isAr: Boolean,
    isOnline: Boolean = true,
    onClose: () -> Unit
) {
    val isLandscape = LocalWindowAdaptiveInfo.current.prefersLandscapeLayout

    val calendar = Calendar.getInstance()
    val dayNames = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")

    val dayOfWeek = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName = monthNames[calendar.get(Calendar.MONTH)]
    val year = calendar.get(Calendar.YEAR)
    val dateString = "$dayOfWeek  $dayOfMonth $monthName $year"

    // Approximate Hijri date
    val hijriMonths = listOf("محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
    val epochMillis = calendar.timeInMillis
    val hijriDaysSinceEpoch = ((epochMillis / 86400000L) - 10643)
    val hijriCycleYears = (hijriDaysSinceEpoch / 10631L) * 30L
    val remainingDays = hijriDaysSinceEpoch % 10631L
    val hijriYear = (hijriCycleYears + (remainingDays / 354L) + 1).toInt()
    val hijriDayInYear = (remainingDays % 354L).toInt()
    val hijriMonth = (hijriDayInYear / 29).coerceIn(0, 11)
    val hijriDay = (hijriDayInYear % 29) + 1
    val hijriString = "$hijriDay ${hijriMonths[hijriMonth]} $hijriYear هـ"

    val arabicPrayerName = prayerNameToArabic(prayerName)

    // ── AI-driven accent for Desk Mode — reads LocalAdaptiveConfig directly
    //    so color changes from AI Brain apply here too without restarting Desk Mode
    val deskAdaptiveConfig = LocalAdaptiveConfig.current
    val deskThemeAccent = resolveAccentFromConfig(deskAdaptiveConfig)
    val accentColor = if (isOnline) deskThemeAccent else AmberZen

    // 12/24 hour format — toggled by AI Brain via is12HourFormat key
    val use12Hour = deskAdaptiveConfig.is12HourFormat

    // Live digital time — LaunchedEffect key includes use12Hour so format
    // switches immediately the moment the AI Brain flips the flag
    var currentTime by remember { mutableStateOf(getCurrentTimeFormatted(use12Hour)) }
    var currentSeconds by remember { mutableStateOf(getCurrentSecondsFormatted()) }
    LaunchedEffect(use12Hour) {
        while (true) {
            currentTime = getCurrentTimeFormatted(use12Hour)
            currentSeconds = getCurrentSecondsFormatted()
            kotlinx.coroutines.delay(1000)
        }
    }

    // Animated subtle pulse
    val infiniteTransition = rememberInfiniteTransition(label = "desktopBg")
    val pulseBright by infiniteTransition.animateFloat(
        initialValue = 0.65f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseBright"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(45000, easing = LinearEasing), RepeatMode.Restart),
        label = "ringRotation"
    )
    val ambientShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "ambientShift"
    )

    // Static particles (deeper, fewer for clean look)
    val particles = remember {
        List(60) {
            SpaceParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 1.8f + 0.4f,
                opacity = Random.nextFloat() * 0.5f + 0.1f
            )
        }
    }

    // ===== ROOT MODAL CONTAINER — FULLY OPAQUE =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000308))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        // Solid deep-space gradient layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.10f),
                            Color(0xFF030812),
                            Color(0xFF000204)
                        ),
                        radius = 1800f
                    )
                )
        )

        // Ambient particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val px = p.x * size.width
                val py = ((p.y + ambientShift * 0.05f) % 1f) * size.height
                drawCircle(
                    color = accentColor.copy(alpha = p.opacity * pulseBright * 0.7f),
                    radius = p.size.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Close button & Connection status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close",
                    tint = accentColor
                )
            }
            Row(
                modifier = Modifier
                    .border(0.75.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(accentColor.copy(alpha = pulseBright), RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) (if (isAr) "متصل" else "ONLINE") else (if (isAr) "غير متصل" else "OFFLINE"),
                    color = accentColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        // ===== MAIN CONTENT =====
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)
            ) {
                // Left Side: Big Clock
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f - 6f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        rotate(ringRotation, pivot = center) {
                            drawCircle(
                                color = accentColor.copy(alpha = 0.20f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f), 0f))
                            )
                        }
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = radius - 18f,
                            center = center,
                            style = Stroke(width = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentTime,
                            color = accentColor,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = accentColor.copy(alpha = 0.55f), blurRadius = 30f))
                        )
                        Text(
                            text = currentSeconds,
                            color = accentColor.copy(alpha = 0.55f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dateString,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = hijriString,
                            color = AmberZen.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Right Side: Prayer Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Next prayer pill
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.75.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isAr) "الصلاة القادمة" else "NEXT PRAYER",
                            color = accentColor.copy(alpha = 0.55f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = arabicPrayerName,
                                color = AmberZen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = AmberZen.copy(alpha = 0.5f), blurRadius = 16f))
                            )
                            Text(
                                text = prayerTime,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(AmberZen.copy(alpha = pulseBright), RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prayerCountdown,
                                color = AmberZen.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "متبقي" else "REMAINING",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Prayer grid
                    if (allPrayerTimes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.025f), RoundedCornerShape(14.dp))
                                .border(0.5.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            val prayerList = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                prayerList.forEach { name ->
                                    PrayerItem(name, allPrayerTimes[name] ?: "--:--", isAr, name == arabicPrayerName)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Portrait Layout (Enhanced for full screen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically)
            ) {
                // Rotating ring & Clock
                Box(
                    modifier = Modifier.size(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f - 6f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        rotate(ringRotation, pivot = center) {
                            drawCircle(
                                color = accentColor.copy(alpha = 0.20f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f), 0f))
                            )
                        }
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = radius - 18f,
                            center = center,
                            style = Stroke(width = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentTime,
                            color = accentColor,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = accentColor.copy(alpha = 0.55f), blurRadius = 30f))
                        )
                        Text(
                            text = currentSeconds,
                            color = accentColor.copy(alpha = 0.55f),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp
                        )
                    }
                }

                // Date strip
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dateString,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hijriString,
                        color = AmberZen.copy(alpha = 0.75f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                }

                // Next prayer pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .border(0.75.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAr) "الصلاة القادمة" else "NEXT PRAYER",
                        color = accentColor.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = arabicPrayerName,
                            color = AmberZen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = AmberZen.copy(alpha = 0.5f), blurRadius = 16f))
                        )
                        Text(
                            text = prayerTime,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(AmberZen.copy(alpha = pulseBright), RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = prayerCountdown,
                            color = AmberZen.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAr) "متبقي" else "REMAINING",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // All Prayer Times grid
                if (allPrayerTimes.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .background(Color.White.copy(alpha = 0.025f), RoundedCornerShape(14.dp))
                            .border(0.5.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                            .padding(vertical = 20.dp, horizontal = 12.dp)
                    ) {
                        val prayerList = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                prayerList.take(3).forEach { name -> PrayerItem(name, allPrayerTimes[name] ?: "--:--", isAr, name == arabicPrayerName) }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                prayerList.drop(3).forEach { name -> PrayerItem(name, allPrayerTimes[name] ?: "--:--", isAr, name == arabicPrayerName) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns formatted time for Desk Mode.
 * [use12Hour] = false → "HH:MM" (24-hour, default)
 * [use12Hour] = true  → "hh:MM AM/PM" (12-hour with meridiem)
 * Controlled by AI Brain via UIConfig.is12HourFormat.
 */
private fun getCurrentTimeFormatted(use12Hour: Boolean = false): String {
    val cal = Calendar.getInstance()
    return if (use12Hour) {
        val rawHour = cal.get(Calendar.HOUR_OF_DAY)
        val hour12  = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute  = cal.get(Calendar.MINUTE)
        val meridiem = if (rawHour >= 12) "PM" else "AM"
        String.format("%02d:%02d %s", hour12, minute, meridiem)
    } else {
        val hour   = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        String.format("%02d:%02d", hour, minute)
    }
}

private fun getCurrentSecondsFormatted(): String {
    val cal    = Calendar.getInstance()
    val second = cal.get(Calendar.SECOND)
    val ampm   = if (cal.get(Calendar.HOUR_OF_DAY) >= 12) "PM" else "AM"
    return String.format("%02d  •  %s", second, ampm)
}
