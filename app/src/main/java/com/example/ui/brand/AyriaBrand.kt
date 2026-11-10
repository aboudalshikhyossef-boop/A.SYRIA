package com.example.ui.brand

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.asyria.v4.R

internal object AyriaColors {
    val abyss = Color(0xFF050A18)
    val deep = Color(0xFF07152B)
    val ocean = Color(0xFF0B2142)
    val panel = Color(0xFF102A50)
    val teal = Color(0xFF16C8F5)
    val glacier = Color(0xFFB9E8FF)
    val ion = Color(0xFF477DFF)
    val pearl = Color(0xFFF1F7FF)
    val muted = Color(0xFF9DB5D4)
    val dim = Color(0xFF637D9F)
    val line = Color(0x3D8EDFFF)
    val glass = Color(0x1A65CFFF)
}

// These bundled files are verified TrueType fonts. The old Cairo resources
// in this repository were HTML error pages masquerading as .ttf files and
// caused ResourcesCompat/Compose to crash while measuring the first Arabic text.
internal val AyriaArabicFont = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

// Amiri is reserved for Quranic text so Arabic letterforms remain traditional and legible.
internal val AyriaQuranFont = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

@Composable
internal fun AyriaAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ayria_ambient")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_drift"
    )

    Box(
        modifier = modifier.background(AyriaColors.abyss)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF081B38),
                        AyriaColors.abyss,
                        Color(0xFF071229)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )

            val glowCenter = Offset(
                x = size.width * (0.18f + 0.10f * drift),
                y = size.height * 0.19f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AyriaColors.teal.copy(alpha = 0.24f),
                        Color(0xFF1C64C7).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = size.maxDimension * 0.66f
                ),
                radius = size.maxDimension * 0.66f,
                center = glowCenter
            )

            val lowerGlow = Offset(size.width * (0.88f - 0.08f * drift), size.height * 0.80f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AyriaColors.ion.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = lowerGlow,
                    radius = size.maxDimension * 0.52f
                ),
                radius = size.maxDimension * 0.52f,
                center = lowerGlow
            )

            val lineColor = AyriaColors.glacier.copy(alpha = 0.035f)
            val arcSize = size.minDimension * 0.76f
            drawArc(
                color = lineColor,
                startAngle = 198f + drift * 5f,
                sweepAngle = 75f,
                useCenter = false,
                topLeft = Offset(
                    x = size.width - arcSize * 0.55f,
                    y = size.height * 0.16f
                ),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.018f),
                start = Offset(size.width * 0.05f, size.height * 0.72f),
                end = Offset(size.width * 0.95f, size.height * 0.42f),
                strokeWidth = 1.dp.toPx()
            )

            // Fine liquid-glass light trails and restrained floating particles.
            drawArc(
                color = AyriaColors.ion.copy(alpha = 0.075f + 0.025f * drift),
                startAngle = 202f - drift * 12f,
                sweepAngle = 112f,
                useCenter = false,
                topLeft = Offset(size.width * 0.58f, size.height * 0.54f),
                size = androidx.compose.ui.geometry.Size(size.minDimension * 0.74f, size.minDimension * 0.74f),
                style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
            )
            val particles = listOf(
                Offset(0.12f, 0.26f), Offset(0.82f, 0.18f),
                Offset(0.73f, 0.62f), Offset(0.20f, 0.78f),
                Offset(0.91f, 0.88f), Offset(0.43f, 0.12f)
            )
            particles.forEachIndexed { index, point ->
                val pulse = 0.42f + 0.28f * kotlin.math.sin((drift * 6.283f) + index * 1.17f)
                val center = Offset(size.width * point.x, size.height * point.y)
                drawCircle(
                    color = AyriaColors.glacier.copy(alpha = pulse.coerceIn(0.08f, 0.78f)),
                    radius = if (index % 2 == 0) 1.35.dp.toPx() else 0.85.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = AyriaColors.teal.copy(alpha = pulse.coerceIn(0.08f, 0.6f) * 0.24f),
                    radius = 5.dp.toPx(),
                    center = center
                )
            }
        }
        content()
    }
}

@Composable
internal fun AyriaBrandMark(
    modifier: Modifier = Modifier,
    contentDescription: String = "A.SYRIA"
) {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
internal fun AyriaGlassPanel(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val fill = if (highlighted) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x2A9CE8E2),
                Color(0x171D5260),
                Color(0x0DFFFFFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x1DFFFFFF),
                Color(0x0CFFFFFF),
                Color(0x0A72D5D0)
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f)
        )
    }
    val edge = if (highlighted) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x99D5FFFA),
                AyriaColors.teal.copy(alpha = 0.46f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.035f),
                AyriaColors.teal.copy(alpha = 0.075f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlighted) 22.dp else 12.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(fill, shape)
            .border(width = 0.65.dp, brush = edge, shape = shape),
        content = content
    )
}

@Composable
internal fun AyriaVerseCard(
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    AyriaGlassPanel(
        modifier = modifier,
        highlighted = true,
        shape = RoundedCornerShape(22.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isArabic) "آيَةٌ وَتَدَبُّر" else "A VERSE TO REFLECT ON",
                color = AyriaColors.teal.copy(alpha = 0.92f),
                fontFamily = AyriaArabicFont,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = if (isArabic) 0.sp else 1.6.sp
            )
            Text(
                text = "وَقُل رَّبِّ زِدْنِي عِلْمًا",
                modifier = Modifier.padding(top = 5.dp),
                color = AyriaColors.pearl,
                fontFamily = AyriaQuranFont,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 31.sp
            )
            Text(
                text = if (isArabic) "سورة طه • الآية ١١٤" else "Taha • 20:114",
                color = AyriaColors.muted,
                fontFamily = AyriaArabicFont,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (!isArabic) {
                Text(
                    text = "My Lord, increase me in knowledge.",
                    color = AyriaColors.glacier.copy(alpha = 0.86f),
                    fontSize = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
internal fun AyriaEyebrow(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.055f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.09f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 13.dp, vertical = 7.dp),
        color = AyriaColors.glacier.copy(alpha = 0.88f),
        fontSize = 10.sp,
        letterSpacing = 1.3.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun AyriaActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false
) {
    val shape = RoundedCornerShape(18.dp)
    val fill = if (secondary) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.085f),
                Color.White.copy(alpha = 0.025f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFB6F0E8),
                AyriaColors.teal,
                Color(0xFF55AEB8)
            )
        )
    }
    val edge = if (secondary) Color.White.copy(alpha = 0.15f) else Color(0x99D9FFFA)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = if (enabled) fill else Brush.linearGradient(
                    listOf(Color(0xFF527F83), Color(0xFF36545F))
                ),
                shape = shape
            )
            .border(1.dp, edge, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (secondary) AyriaColors.pearl else Color(0xFF06212A),
            fontFamily = AyriaArabicFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * Continuous five-panel glacier/water panorama shared by the five primary tabs.
 * Switching tabs reveals the next crop of the same scene instead of restarting
 * an unrelated background. Gyroscope input adds a restrained parallax response.
 */
@Composable
internal fun AyriaPanoramaBackdrop(
    pageIndex: Int,
    roll: Float = 0f,
    pitch: Float = 0f,
    modifier: Modifier = Modifier
) {
    val pageProgress by animateFloatAsState(
        targetValue = pageIndex.coerceIn(0, 4).toFloat(),
        animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing),
        label = "ayria_panorama_page"
    )
    val atmosphere = rememberInfiniteTransition(label = "ayria_panorama_atmosphere")
    val shimmer by atmosphere.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 12000), RepeatMode.Reverse),
        label = "ayria_panorama_shimmer"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val viewportWidth = size.width
        val worldWidth = viewportWidth * 5f
        val cameraX = viewportWidth * pageProgress + roll.coerceIn(-20f, 20f) * viewportWidth * 0.012f
        val cameraY = pitch.coerceIn(-20f, 20f) * size.height * 0.009f
        val height = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF07182F), Color(0xFF061426), Color(0xFF0A2942),
                    Color(0xFF06152B), Color(0xFF040D1D)
                ),
                start = Offset(-cameraX, 0f),
                end = Offset(worldWidth - cameraX, height)
            )
        )

        for (panel in 0..4) {
            val center = Offset(
                x = viewportWidth * (panel + 0.48f) - cameraX,
                y = height * (0.22f + (panel % 2) * 0.21f) + cameraY
            )
            val glowRadius = viewportWidth * 0.78f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x553EBFE8), Color(0x1D267CB9), Color.Transparent),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )
        }

        val ridgeHeights = listOf(0.31f, 0.25f, 0.36f, 0.27f, 0.33f)
        for (panel in 0..4) {
            val centerX = viewportWidth * (panel + 0.5f) - cameraX
            val peak = Offset(centerX + viewportWidth * 0.02f, height * ridgeHeights[panel] + cameraY)
            val left = Offset(centerX - viewportWidth * 0.52f, height * 0.79f + cameraY)
            val right = Offset(centerX + viewportWidth * 0.56f, height * 0.81f + cameraY)
            val facet = androidx.compose.ui.graphics.Path().apply {
                moveTo(left.x, left.y)
                lineTo(peak.x, peak.y)
                lineTo(right.x, right.y)
                lineTo(centerX + viewportWidth * 0.1f, height * 0.91f + cameraY)
                close()
            }
            drawPath(
                path = facet,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x2737A9D5), Color(0x102A6798), Color(0x248DE4F4)),
                    start = peak,
                    end = right
                )
            )
            drawPath(
                path = facet,
                color = Color(0x5CA8E8F7).copy(alpha = 0.11f + shimmer * 0.035f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        for (ribbon in 0..3) {
            val path = androidx.compose.ui.graphics.Path()
            val baseY = height * (0.61f + ribbon * 0.075f) + cameraY
            path.moveTo(-cameraX, baseY)
            for (segment in 0..4) {
                val startX = viewportWidth * segment - cameraX
                val endX = startX + viewportWidth
                val wave = sin((segment * 0.86f + ribbon * 1.21f + shimmer * 0.12f).toDouble()).toFloat()
                path.cubicTo(
                    startX + viewportWidth * 0.27f, baseY + wave * height * 0.035f,
                    startX + viewportWidth * 0.73f, baseY - wave * height * 0.045f,
                    endX, baseY + wave * height * 0.018f
                )
            }
            drawPath(
                path = path,
                color = if (ribbon % 2 == 0) Color(0xFF8ADCF5).copy(alpha = 0.055f + shimmer * 0.025f)
                else Color.White.copy(alpha = 0.025f + shimmer * 0.012f),
                style = Stroke(width = if (ribbon == 0) 2.dp.toPx() else 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        for (point in 0..26) {
            val worldX = worldWidth * (point / 26f)
            val localX = worldX - cameraX
            val y = height * (0.13f + ((point * 37) % 71) / 100f) + cameraY * 0.5f
            val alpha = 0.10f + 0.10f * (0.5f + 0.5f * sin((shimmer * 6.283f + point).toDouble()).toFloat())
            drawCircle(
                color = Color(0xFFD9F7FF).copy(alpha = alpha),
                radius = if (point % 3 == 0) 1.4.dp.toPx() else 0.8.dp.toPx(),
                center = Offset(localX, y)
            )
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.20f), Color.Transparent, Color.Black.copy(alpha = 0.36f))
            )
        )
    }
}
