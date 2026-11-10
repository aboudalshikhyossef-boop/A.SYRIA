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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.ChevronLeft
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
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    val isAr by viewModel.isArabic.collectAsState()
    val isStealthMode by viewModel.isStealthMode.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    val isNeuralLinkOffline by viewModel.isNeuralLinkOffline.collectAsState()
    val isVaultViewOpen by viewModel.isVaultViewOpen.collectAsState()

    val nextPrayerName by prayerViewModel.nextPrayerName.collectAsState()
    val nextPrayerCountdown by prayerViewModel.nextPrayerCountdown.collectAsState()
    val nextPrayerProgress by prayerViewModel.nextPrayerProgress.collectAsState()
    val nextPrayerTime by prayerViewModel.nextPrayerTime.collectAsState()
    val allPrayerTimes by prayerViewModel.allPrayerTimes.collectAsState()

    var isDesktopModeOpen by remember { mutableStateOf(false) }
    var currentVerseIndex by remember { mutableStateOf(0) }
    val easing = remember { CubicBezierEasing(0.445f, 0.05f, 0.55f, 0.95f) }
    val accent = if (isStealthMode) Color(0xFF9DB8CC) else AyriaColors.teal

    val spiritualVerses = remember(isAr) {
        if (isAr) listOf(
            "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            "يَهْدِي اللَّهُ لِنُورِهِ مَنْ يَشَاءُ",
            "وَكَانَ حَقًّا عَلَيْنَا نَصْرُ الْمُؤْمِنِينَ"
        ) else listOf(
            "Verily, in the remembrance of Allah do hearts find rest",
            "Indeed, with hardship comes ease",
            "And whoever relies upon Allah - then He is sufficient for him",
            "Allah guides to His light whom He wills",
            "And it was due from Us to aid the believers"
        )
    }

    BackHandler(enabled = isDesktopModeOpen || isVaultViewOpen) {
        if (isDesktopModeOpen) isDesktopModeOpen = false else viewModel.setVaultViewOpen(false)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardGlassAction(
                        icon = Icons.Rounded.Lock,
                        title = if (isAr) "الخزينة" else "Vault",
                        subtitle = if (isAr) "حفظ مشفّر" else "Encrypted storage",
                        accent = accent,
                        modifier = Modifier.weight(1f).height(126.dp),
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            if (viewModel.isVaultAuthenticated.value) {
                                viewModel.setVaultViewOpen(true)
                            } else {
                                onVaultLockRequest(
                                    if (isAr) "تأكيد الهوية" else "VERIFY IDENTITY",
                                    if (isAr) "يلزم التحقق للوصول إلى الخزينة الآمنة" else "Verification is required to open the secure vault",
                                    {
                                        viewModel.setVaultAuthenticated(true)
                                        viewModel.setVaultViewOpen(true)
                                    }
                                )
                            }
                        }
                    )
                    DashboardGlassAction(
                        icon = Icons.Rounded.Wifi,
                        title = if (isAr) "عنوان IP" else "Network IP",
                        subtitle = ipAddress.ifBlank { if (isAr) "بانتظار الشبكة" else "Waiting for network" },
                        accent = accent,
                        modifier = Modifier.weight(1f).height(126.dp),
                        onClick = {
                            if (ipAddress.isNotBlank()) {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(ipAddress))
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }

                DashboardGlassAction(
                    icon = Icons.Rounded.Explore,
                    title = if (isAr) "المصادر والفحص" else "Sources & Scan",
                    subtitle = if (isAr) "فحص الروابط والملفات" else "Inspect links and files",
                    accent = accent,
                    modifier = Modifier.fillMaxWidth().height(94.dp),
                    horizontal = true,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onNavigateToScanner()
                    }
                )

                AyriaGlassPanel(
                    modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 132.dp),
                    highlighted = true,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        NeuralVerseModule(
                            verse = spiritualVerses[currentVerseIndex],
                            easing = easing,
                            onTap = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                currentVerseIndex = (currentVerseIndex + 1) % spiritualVerses.size
                            }
                        )
                    }
                }

                DashboardGlassAction(
                    icon = Icons.Rounded.DesktopWindows,
                    title = if (isAr) "وضع المكتب" else "Desktop Mode",
                    subtitle = if (isAr) "واجهة موسّعة للأدوات" else "Expanded workspace",
                    accent = accent,
                    modifier = Modifier.fillMaxWidth().height(76.dp),
                    horizontal = true,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        isDesktopModeOpen = true
                    }
                )
            }

            if (isVaultViewOpen) {
                SecureVaultScreen(viewModel = viewModel, isArabic = isAr, onClose = { viewModel.setVaultViewOpen(false) })
            }

            if (isDesktopModeOpen) {
                DesktopModeScreen(
                    prayerName = nextPrayerName,
                    prayerTime = nextPrayerTime,
                    prayerCountdown = nextPrayerCountdown,
                    allPrayerTimes = allPrayerTimes,
                    isAr = isAr,
                    isOnline = !isNeuralLinkOffline,
                    nextPrayerProgress = nextPrayerProgress,
                    onClose = { isDesktopModeOpen = false }
                )
            }
        }
    }
}

@Composable
private fun DashboardGlassAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    onClick: () -> Unit
) {
    AyriaGlassPanel(
        modifier = modifier.clickable(onClick = onClick),
        highlighted = true,
        shape = RoundedCornerShape(24.dp)
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(46.dp)
                        .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.25f), Color(0x1EFFFFFF))), CircleShape)
                        .border(0.8.dp, accent.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = AyriaColors.glacier, modifier = Modifier.size(23.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, color = AyriaColors.pearl, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AyriaArabicFont)
                    Text(subtitle, color = AyriaColors.muted, fontSize = 10.sp, fontFamily = AyriaArabicFont, maxLines = 1)
                }
                Icon(Icons.Rounded.ChevronLeft, null, tint = accent.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(42.dp)
                        .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.25f), Color(0x1EFFFFFF))), CircleShape)
                        .border(0.8.dp, accent.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = AyriaColors.glacier, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(7.dp))
                Text(title, color = AyriaColors.pearl, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AyriaArabicFont, maxLines = 1)
                Text(subtitle, color = AyriaColors.muted, fontSize = 9.sp, fontFamily = AyriaArabicFont, maxLines = 1)
            }
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
    nextPrayerProgress: Float = 0f,
    onClose: () -> Unit
) {
    val isLandscape = LocalWindowAdaptiveInfo.current.prefersLandscapeLayout
    val adaptiveConfig = LocalAdaptiveConfig.current
    val use12Hour = adaptiveConfig.is12HourFormat
    var currentTime by remember { mutableStateOf(getCurrentTimeFormatted(use12Hour)) }
    var currentSeconds by remember { mutableStateOf(getCurrentSecondsFormatted()) }
    var selectedReflection by remember { mutableStateOf(0) }

    LaunchedEffect(use12Hour) {
        while (true) {
            currentTime = getCurrentTimeFormatted(use12Hour)
            currentSeconds = getCurrentSecondsFormatted()
            kotlinx.coroutines.delay(1000)
        }
    }

    val atmosphere = rememberInfiniteTransition(label = "polar_desk_atmosphere")
    val auroraDrift by atmosphere.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "polar_aurora_drift"
    )

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    androidx.compose.runtime.DisposableEffect(activity) {
        val window = activity?.window
        val alreadyKeptOn = window?.attributes?.flags
            ?.and(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            if (window != null && alreadyKeptOn != true) {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val todayLabel = remember(isAr) {
        val locale = if (isAr) java.util.Locale("ar") else java.util.Locale.getDefault()
        java.text.SimpleDateFormat(if (isAr) "EEEE، d MMMM yyyy" else "EEEE, d MMMM yyyy", locale)
            .format(java.util.Date())
    }
    val arabicPrayerName = prayerNameToArabic(prayerName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071426),
                        Color(0xFF0B2035),
                        Color(0xFF061322),
                        Color(0xFF030B16)
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val glowOne = Offset(width * (0.12f + auroraDrift * 0.10f), height * 0.12f)
            val glowTwo = Offset(width * (0.88f - auroraDrift * 0.08f), height * 0.56f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x223FBDD5), Color(0x102C83A6), Color.Transparent),
                    center = glowOne,
                    radius = width * 0.82f
                ),
                radius = width * 0.82f,
                center = glowOne
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1D80D6D0), Color(0x0A477F9B), Color.Transparent),
                    center = glowTwo,
                    radius = width * 0.64f
                ),
                radius = width * 0.64f,
                center = glowTwo
            )
            for (index in 0..30) {
                val x = width * ((index * 37 % 101) / 100f)
                val y = height * ((index * 61 % 97) / 100f)
                val alpha = 0.06f + 0.05f * (0.5f + 0.5f * sin(auroraDrift * 6.283f + index))
                drawCircle(
                    color = Color(0xFFD9F5FF).copy(alpha = alpha),
                    radius = if (index % 5 == 0) 1.25.dp.toPx() else 0.75.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            drawLine(
                color = Color(0xFFB9ECF5).copy(alpha = 0.045f),
                start = Offset(0f, height * 0.76f),
                end = Offset(width, height * 0.63f),
                strokeWidth = 1.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isLandscape) 28.dp else 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        if (isAr) "وضع المكتب" else "POLAR DESK",
                        color = Color(0xFFD7F2FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.4.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        if (isAr) "مساحة هادئة للتركيز والقراءة" else "A quiet space for focus, reading and work",
                        color = Color(0xFFA8C5D3).copy(alpha = 0.66f),
                        fontSize = 10.sp,
                        fontFamily = AyriaArabicFont
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isOnline) Color(0xFF9BE5D5) else Color(0xFF8AA6B8),
                                CircleShape
                            )
                    )
                    Text(
                        if (isOnline) (if (isAr) "متصل" else "CONNECTED") else (if (isAr) "دون اتصال" else "OFFLINE"),
                        color = Color(0xFFB8D0DA).copy(alpha = 0.62f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isAr) "إغلاق وضع المكتب" else "Close desk mode",
                            tint = Color(0xFFD6EEF7)
                        )
                    }
                }
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeskClockPanel(
                        modifier = Modifier.weight(1f),
                        time = currentTime,
                        seconds = currentSeconds,
                        date = todayLabel,
                        isArabic = isAr
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrayerCountdownWheel(
                            prayerName = arabicPrayerName,
                            prayerTime = prayerTime,
                            countdown = prayerCountdown,
                            progress = nextPrayerProgress,
                            isArabic = isAr,
                            diameter = 190.dp
                        )
                        DeskPrayerSchedule(allPrayerTimes, arabicPrayerName, isAr)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeskClockPanel(
                        modifier = Modifier.weight(1f),
                        time = currentTime,
                        seconds = currentSeconds,
                        date = todayLabel,
                        isArabic = isAr,
                        compact = true
                    )
                    PrayerCountdownWheel(
                        prayerName = arabicPrayerName,
                        prayerTime = prayerTime,
                        countdown = prayerCountdown,
                        progress = nextPrayerProgress,
                        isArabic = isAr,
                        diameter = 164.dp
                    )
                }
                DeskPrayerSchedule(allPrayerTimes, arabicPrayerName, isAr)
            }

            DeskReflectionPanel(
                selectedTab = selectedReflection,
                onSelectTab = { selectedReflection = it },
                isArabic = isAr
            )

            Text(
                if (isAr) "خُذ نفساً هادئاً، ودع الوقت يمضي بطمأنينة." else "A little stillness makes room for better work.",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFB7D3DF).copy(alpha = 0.42f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = AyriaArabicFont
            )
        }
    }
}

@Composable
private fun DeskClockPanel(
    modifier: Modifier,
    time: String,
    seconds: String,
    date: String,
    isArabic: Boolean,
    compact: Boolean = false
) {
    DeskFrostPanel(modifier = modifier) {
        Text(
            if (isArabic) "الوقت الآن" else "LOCAL TIME",
            color = Color(0xFFB8D8E5).copy(alpha = 0.60f),
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(8.dp))
        Text(
            time,
            color = Color(0xFFE5F7FF),
            fontSize = if (compact) 34.sp else 54.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = if (compact) 0.sp else 2.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Text(
            seconds,
            color = Color(0xFFA9C8D8).copy(alpha = 0.64f),
            fontSize = if (compact) 10.sp else 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            date,
            color = Color(0xFFCEE6EF).copy(alpha = 0.72f),
            fontSize = if (compact) 10.sp else 13.sp,
            textAlign = TextAlign.Center,
            fontFamily = AyriaArabicFont,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            if (isArabic) "A.SYRIA · POLAR TIME" else "A.SYRIA · POLAR TIME",
            color = Color(0xFF9CBECB).copy(alpha = 0.34f),
            fontSize = 7.sp,
            letterSpacing = 1.4.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
internal fun PrayerCountdownWheel(
    prayerName: String,
    prayerTime: String,
    countdown: String,
    progress: Float,
    isArabic: Boolean,
    diameter: androidx.compose.ui.unit.Dp
) {
    val remaining = (1f - progress).coerceIn(0f, 1f)
    Box(
        modifier = Modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val inset = stroke / 2f + 2.dp.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Color(0xFFB8D4E1).copy(alpha = 0.13f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFBFEAF3),
                        Color(0xFF7BB7CB),
                        Color(0xFFB7D7EA),
                        Color(0xFFBFEAF3)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * remaining,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            for (index in 0 until 60 step 5) {
                val angle = Math.toRadians((index * 6 - 90).toDouble())
                val radius = size.minDimension * 0.47f
                val center = Offset(
                    size.width / 2f + kotlin.math.cos(angle).toFloat() * radius,
                    size.height / 2f + kotlin.math.sin(angle).toFloat() * radius
                )
                drawCircle(
                    color = Color(0xFFC8E7F0).copy(alpha = 0.34f),
                    radius = 1.dp.toPx(),
                    center = center
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                if (isArabic) "الصلاة القادمة" else "NEXT PRAYER",
                color = Color(0xFFAFCBD8).copy(alpha = 0.56f),
                fontSize = 8.sp,
                letterSpacing = 1.3.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(4.dp))
            Text(
                prayerName,
                color = Color(0xFFE7F6FC),
                fontSize = if (diameter < 180.dp) 17.sp else 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AyriaArabicFont
            )
            Text(
                prayerTime,
                color = Color(0xFFB8D7E3).copy(alpha = 0.76f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(6.dp))
            Text(
                countdown,
                color = Color(0xFFE7F6FC),
                fontSize = if (diameter < 180.dp) 13.sp else 15.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                if (isArabic) "متبقٍ" else "REMAINING",
                color = Color(0xFFAFCBD8).copy(alpha = 0.48f),
                fontSize = 7.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
internal fun DeskPrayerSchedule(
    prayerTimes: Map<String, String>,
    nextPrayer: String,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val prayerNames = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
    DeskFrostPanel(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isArabic) "مواقيت اليوم" else "TODAY'S PRAYER TIMES",
                color = Color(0xFFD5EDF6),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Monospace
            )
            Text(
                if (isArabic) "حسب الموقع المتاح" else "LOCAL CALCULATION",
                color = Color(0xFF9CBBC9).copy(alpha = 0.50f),
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.height(9.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prayerNames.chunked(3).forEach { rowNames ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowNames.forEach { name ->
                        val selected = name == nextPrayer
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) Color(0x2478C1D8) else Color.White.copy(alpha = 0.035f),
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    0.7.dp,
                                    if (selected) Color(0x667FBFD4) else Color.White.copy(alpha = 0.06f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                name,
                                color = if (selected) Color(0xFFE0F7FF) else Color(0xFFAFC8D6),
                                fontSize = 9.sp,
                                fontFamily = AyriaArabicFont
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                prayerTimes[name] ?: "--:--",
                                color = if (selected) Color.White else Color(0xFFC7DDE6).copy(alpha = 0.72f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeskReflectionPanel(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    isArabic: Boolean
) {
    DeskFrostPanel(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (isArabic) "لحظة من السكينة" else "A MOMENT OF STILLNESS",
                color = Color(0xFFD5EDF6),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(if (isArabic) "دعاء" else "DUA", if (isArabic) "حديث" else "HADITH").forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) Color(0x447ABCD1) else Color.White.copy(alpha = 0.035f),
                                CircleShape
                            )
                            .border(0.8.dp, Color.White.copy(alpha = if (selected) 0.22f else 0.07f), CircleShape)
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) Color(0xFFE0F5FC) else Color(0xFF9DB8C7),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Monospace
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(11.dp))
        if (selectedTab == 0) {
            Text(
                "رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي ۝ وَاحْلُلْ عُقْدَةً مِنْ لِسَانِي ۝ يَفْقَهُوا قَوْلِي",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE6F6FC),
                fontSize = 16.sp,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
                fontFamily = AyriaQuranFont
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (isArabic) "سورة طه · الآيات ٢٥–٢٨" else "Qur'an · Taha 20:25–28",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFA5C5D3).copy(alpha = 0.62f),
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Text(
                "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE6F6FC),
                fontSize = 17.sp,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
                fontFamily = AyriaArabicFont
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (isArabic) "صحيح البخاري · حديث ١" else "Sahih al-Bukhari · 1",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFA5C5D3).copy(alpha = 0.62f),
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
internal fun DeskFrostPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(Color(0x2D9DD6E8), Color(0x191F536B), Color(0x15FFFFFF))
                ),
                RoundedCornerShape(24.dp)
            )
            .border(
                0.8.dp,
                Brush.linearGradient(
                    listOf(Color(0x779CD8EA), Color(0x2848A0BC), Color(0x22FFFFFF))
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(14.dp),
        content = content
    )
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
