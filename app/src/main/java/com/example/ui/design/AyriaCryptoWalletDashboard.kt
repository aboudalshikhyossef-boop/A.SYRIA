package com.example.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.brand.AyriaArabicFont
import kotlinx.coroutines.delay

/**
 * Crypto-wallet-inspired dashboard presentation built exclusively from the
 * shared ui.design glass primitives. All displayed values are supplied by the
 * existing ViewModels; this component owns no business or persistence logic.
 */
@Composable
fun AyriaCryptoWalletDashboard(
    isArabic: Boolean,
    cyberScore: Int,
    cyberRank: String,
    cyberProgress: Float,
    isStealthMode: Boolean,
    isNetworkOffline: Boolean,
    ipAddress: String,
    batteryPercentage: Int,
    isVaultAuthenticated: Boolean,
    verse: String,
    onOpenVault: () -> Unit,
    onCopyIp: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenDesktop: () -> Unit,
    onVerseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < 350.dp
        val horizontalInset = if (compact) 14.dp else 18.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = horizontalInset, vertical = 12.dp)
                .padding(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 13.dp else 16.dp)
        ) {
            StaggeredReveal(delayMillis = 0) {
                WalletHeader(isArabic = isArabic, isNetworkOffline = isNetworkOffline)
            }

            StaggeredReveal(delayMillis = 65) {
                WalletHeroCard(
                    isArabic = isArabic,
                    cyberScore = cyberScore,
                    cyberRank = cyberRank,
                    cyberProgress = cyberProgress,
                    isStealthMode = isStealthMode,
                    isNetworkOffline = isNetworkOffline,
                    compact = compact
                )
            }

            StaggeredReveal(delayMillis = 135) {
                WalletQuickActions(
                    isArabic = isArabic,
                    ipAvailable = ipAddress.isNotBlank(),
                    onOpenVault = onOpenVault,
                    onCopyIp = onCopyIp,
                    onOpenScanner = onOpenScanner,
                    onOpenDesktop = onOpenDesktop
                )
            }

            StaggeredReveal(delayMillis = 215) {
                WalletMetrics(
                    isArabic = isArabic,
                    cyberScore = cyberScore,
                    cyberProgress = cyberProgress,
                    batteryPercentage = batteryPercentage,
                    isNetworkOffline = isNetworkOffline
                )
            }

            StaggeredReveal(delayMillis = 295) {
                WalletActivity(
                    isArabic = isArabic,
                    isNetworkOffline = isNetworkOffline,
                    ipAddress = ipAddress,
                    isVaultAuthenticated = isVaultAuthenticated,
                    isStealthMode = isStealthMode,
                    verse = verse,
                    onVerseClick = onVerseClick
                )
            }
        }
    }
}

@Composable
private fun WalletHeader(
    isArabic: Boolean,
    isNetworkOffline: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "A.SYRIA",
                color = AyriaDesignTokens.Colors.glacier,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isArabic) "محفظتك السيبرانية" else "YOUR CYBER WALLET",
                color = AyriaDesignTokens.Colors.pearl,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AyriaArabicFont
            )
        }

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(AyriaDesignTokens.Colors.panel.copy(alpha = 0.66f))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(
                        if (isNetworkOffline) AyriaDesignTokens.Colors.muted
                        else AyriaDesignTokens.Colors.teal,
                        CircleShape
                    )
            )
            Text(
                text = if (isNetworkOffline) {
                    if (isArabic) "غير متصل" else "OFFLINE"
                } else {
                    if (isArabic) "العقدة نشطة" else "NODE ONLINE"
                },
                color = AyriaDesignTokens.Colors.muted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WalletHeroCard(
    isArabic: Boolean,
    cyberScore: Int,
    cyberRank: String,
    cyberProgress: Float,
    isStealthMode: Boolean,
    isNetworkOffline: Boolean,
    compact: Boolean
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        level = GlassLevel.Elevated,
        backdropContent = {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AyriaDesignTokens.Colors.ocean.copy(alpha = 0.78f),
                            AyriaDesignTokens.Colors.deep.copy(alpha = 0.54f),
                            AyriaDesignTokens.Colors.abyss.copy(alpha = 0.84f)
                        ),
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AyriaDesignTokens.Colors.teal.copy(alpha = 0.30f),
                            AyriaDesignTokens.Colors.ion.copy(alpha = 0.11f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.16f),
                        radius = size.minDimension * 0.80f
                    ),
                    radius = size.minDimension * 0.80f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.16f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "رصيدك السيبراني" else "CYBER SCORE",
                    color = AyriaDesignTokens.Colors.glacier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isStealthMode) {
                        if (isArabic) "وضع التخفي نشط" else "STEALTH ACTIVE"
                    } else {
                        if (isArabic) "الحماية القياسية" else "STANDARD MODE"
                    },
                    color = AyriaDesignTokens.Colors.pearl.copy(alpha = 0.88f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = cyberScore.toString(),
                            color = AyriaDesignTokens.Colors.pearl,
                            fontSize = if (compact) 38.sp else 44.sp,
                            lineHeight = if (compact) 42.sp else 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) " نقطة" else " PTS",
                            color = AyriaDesignTokens.Colors.muted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                        )
                    }
                    Text(
                        text = cyberRank,
                        color = AyriaDesignTokens.Colors.pearl.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AyriaArabicFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isArabic) "التقدم نحو الرتبة التالية" else "PROGRESS TO NEXT RANK",
                        color = AyriaDesignTokens.Colors.muted,
                        fontSize = 9.sp
                    )
                }

                ProgressRing(
                    progress = cyberProgress,
                    size = if (compact) 88.dp else 104.dp,
                    strokeWidth = 7.dp,
                    label = if (isArabic) "التقدم" else "RANK",
                    valueLabel = "${(cyberProgress.coerceIn(0f, 1f) * 100).toInt()}%"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AyriaDesignTokens.Colors.abyss.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "حالة الشبكة" else "NETWORK STATUS",
                    color = AyriaDesignTokens.Colors.muted,
                    fontSize = 9.sp
                )
                Text(
                    text = if (isNetworkOffline) {
                        if (isArabic) "غير متصل" else "OFFLINE"
                    } else {
                        if (isArabic) "الاتصال متاح" else "CONNECTED"
                    },
                    color = if (isNetworkOffline) AyriaDesignTokens.Colors.muted else AyriaDesignTokens.Colors.glacier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WalletQuickActions(
    isArabic: Boolean,
    ipAvailable: Boolean,
    onOpenVault: () -> Unit,
    onCopyIp: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenDesktop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top
    ) {
        WalletQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Lock,
            label = if (isArabic) "الخزينة" else "Vault",
            onClick = onOpenVault
        )
        WalletQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Wifi,
            label = if (isArabic) "نسخ IP" else "Copy IP",
            enabled = ipAvailable,
            onClick = onCopyIp
        )
        WalletQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Explore,
            label = if (isArabic) "فحص" else "Scan",
            onClick = onOpenScanner
        )
        WalletQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.DesktopWindows,
            label = if (isArabic) "المكتب" else "Desktop",
            onClick = onOpenDesktop
        )
    }
}

@Composable
private fun WalletQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        GlassSurface(
            modifier = Modifier
                .size(58.dp)
                .clickable(enabled = enabled, onClick = onClick),
            shape = CircleShape,
            level = if (enabled) GlassLevel.Elevated else GlassLevel.Subtle
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) AyriaDesignTokens.Colors.glacier else AyriaDesignTokens.Colors.dim,
                    modifier = Modifier.size(23.dp)
                )
            }
        }
        Text(
            text = label,
            color = if (enabled) AyriaDesignTokens.Colors.pearl else AyriaDesignTokens.Colors.dim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = AyriaArabicFont,
            maxLines = 1
        )
    }
}

@Composable
private fun WalletMetrics(
    isArabic: Boolean,
    cyberScore: Int,
    cyberProgress: Float,
    batteryPercentage: Int,
    isNetworkOffline: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionHeader(
            title = if (isArabic) "نظرة سريعة" else "QUICK OVERVIEW",
            subtitle = if (isArabic) "لقطة مباشرة من حالة الجهاز" else "Live device snapshot"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WalletMetricCard(
                modifier = Modifier.weight(1f),
                title = if (isArabic) "النقاط" else "SCORE",
                value = cyberScore.toString(),
                caption = if (isArabic) "حالي" else "CURRENT",
                pulseLevel = cyberProgress
            )
            WalletMetricCard(
                modifier = Modifier.weight(1f),
                title = if (isArabic) "البطارية" else "BATTERY",
                value = if (batteryPercentage in 1..100) "$batteryPercentage%" else "—",
                caption = if (isArabic) "مستوى الشحن" else "CHARGE LEVEL",
                pulseLevel = (batteryPercentage.coerceIn(0, 100) / 100f)
            )
            WalletMetricCard(
                modifier = Modifier.weight(1f),
                title = if (isArabic) "الشبكة" else "NETWORK",
                value = if (isNetworkOffline) {
                    if (isArabic) "موقوف" else "OFF"
                } else {
                    if (isArabic) "نشطة" else "ON"
                },
                caption = if (isArabic) "حالة مباشرة" else "LIVE STATUS",
                pulseLevel = if (isNetworkOffline) 0f else 1f
            )
        }
    }
}

@Composable
private fun WalletMetricCard(
    title: String,
    value: String,
    caption: String,
    pulseLevel: Float,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        level = GlassLevel.Standard,
        contentPadding = 10.dp
    ) {
        Text(
            text = title,
            color = AyriaDesignTokens.Colors.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = value,
            color = AyriaDesignTokens.Colors.pearl,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(7.dp))
        WalletSparkline(
            tint = AyriaDesignTokens.Colors.glacier,
            modifier = Modifier
                .fillMaxWidth()
                .height(17.dp),
            level = pulseLevel
        )
        Text(
            text = caption,
            color = AyriaDesignTokens.Colors.dim,
            fontSize = 8.sp,
            maxLines = 1
        )
    }
}

/**
 * A neutral snapshot trace, not a fabricated time-series. The current metric
 * controls the trace's vertical position and live endpoint; no historical data
 * is implied because the app does not yet expose metric history.
 */
@Composable
private fun WalletSparkline(
    tint: Color,
    level: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val value = level.coerceIn(0f, 1f)
        val y = size.height * (0.72f - value * 0.34f)
        val path = Path().apply {
            moveTo(0f, y)
            lineTo(size.width, y)
        }
        drawPath(
            path = path,
            color = tint.copy(alpha = 0.13f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(tint.copy(alpha = 0.24f), tint.copy(alpha = 0.78f))
            ),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = tint.copy(alpha = 0.9f),
            radius = 2.4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(size.width, y)
        )
    }
}

@Composable
private fun WalletActivity(
    isArabic: Boolean,
    isNetworkOffline: Boolean,
    ipAddress: String,
    isVaultAuthenticated: Boolean,
    isStealthMode: Boolean,
    verse: String,
    onVerseClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionHeader(
            title = if (isArabic) "النشاط الحالي" else "LIVE SYSTEM ACTIVITY",
            subtitle = if (isArabic) "حالات مباشرة من خدمات التطبيق" else "Current service states"
        )
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            level = GlassLevel.Standard,
            contentPadding = 0.dp
        ) {
            WalletActivityRow(
                icon = Icons.Rounded.Wifi,
                title = if (isArabic) "اتصال الشبكة" else "Network connection",
                description = ipAddress.ifBlank {
                    if (isNetworkOffline) {
                        if (isArabic) "لا يوجد اتصال متاح" else "No active connection"
                    } else {
                        if (isArabic) "بانتظار عنوان IP" else "Waiting for IP address"
                    }
                },
                status = if (isNetworkOffline) {
                    if (isArabic) "غير متصل" else "OFFLINE"
                } else {
                    if (isArabic) "متصل" else "ONLINE"
                },
                accent = !isNetworkOffline
            )
            WalletActivityDivider()
            WalletActivityRow(
                icon = Icons.Rounded.Lock,
                title = if (isArabic) "الخزينة الآمنة" else "Secure vault",
                description = if (isVaultAuthenticated) {
                    if (isArabic) "تم فتحها لهذه الجلسة" else "Unlocked for this session"
                } else {
                    if (isArabic) "مقفلة وتتطلب التحقق" else "Locked — verification required"
                },
                status = if (isVaultAuthenticated) {
                    if (isArabic) "مفتوحة" else "OPEN"
                } else {
                    if (isArabic) "مقفلة" else "LOCKED"
                },
                accent = !isVaultAuthenticated
            )
            WalletActivityDivider()
            WalletActivityRow(
                icon = Icons.Rounded.Lock,
                title = if (isArabic) "وضع التخفي" else "Stealth mode",
                description = if (isStealthMode) {
                    if (isArabic) "الواجهة في وضع التخفي" else "Stealth presentation is enabled"
                } else {
                    if (isArabic) "الوضع القياسي للتطبيق" else "Standard app presentation"
                },
                status = if (isStealthMode) {
                    if (isArabic) "نشط" else "ACTIVE"
                } else {
                    if (isArabic) "عادي" else "STANDARD"
                },
                accent = isStealthMode
            )
            WalletActivityDivider()
            WalletActivityRow(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = if (isArabic) "ومضة اليوم" else "Daily reflection",
                description = verse,
                status = if (isArabic) "تبديل" else "CYCLE",
                accent = true,
                onClick = onVerseClick
            )
        }
    }
}

@Composable
private fun WalletActivityRow(
    icon: ImageVector,
    title: String,
    description: String,
    status: String,
    accent: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AyriaDesignTokens.Colors.glacier.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AyriaDesignTokens.Colors.glacier,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = AyriaDesignTokens.Colors.pearl,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AyriaArabicFont
            )
            Text(
                text = description,
                color = AyriaDesignTokens.Colors.muted,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                maxLines = if (onClick != null) 3 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = status,
            color = if (accent) AyriaDesignTokens.Colors.glacier else AyriaDesignTokens.Colors.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun WalletActivityDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 12.dp)
            .height(1.dp)
            .background(AyriaDesignTokens.Colors.line.copy(alpha = 0.45f))
    )
}

@Composable
private fun StaggeredReveal(
    delayMillis: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0L) delay(delayMillis)
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "dashboard-stagger-alpha"
    )
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else 12f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "dashboard-stagger-offset"
    )
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offset.dp.toPx()
        }
    ) {
        content()
    }
}
