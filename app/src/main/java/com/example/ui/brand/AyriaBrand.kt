package com.example.ui.brand

import kotlin.math.roundToInt
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.BoxWithConstraints
import android.util.Base64
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.remember
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
 * Displays the supplied chrome-glacier image as one continuous five-panel panorama.
 * Each main tab shows an adjacent crop of the same image; gyro values add subtle parallax.
 */
@Composable
internal fun AyriaPanoramaBackdrop(
    pageIndex: Int,
    roll: Float = 0f,
    pitch: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val panoramaBitmap = remember(context) {
        val resourceParts = intArrayOf(
            R.raw.asyria_panorama_part0,
            R.raw.asyria_panorama_part1,
            R.raw.asyria_panorama_part2,
            R.raw.asyria_panorama_part3,
            R.raw.asyria_panorama_part4,
            R.raw.asyria_panorama_part5,
            R.raw.asyria_panorama_part6,
            R.raw.asyria_panorama_part7
        )
        val encodedImage = buildString {
            resourceParts.forEach { resourceId ->
                context.resources.openRawResource(resourceId).bufferedReader(Charsets.US_ASCII).use { reader ->
                    append(reader.readText().trim())
                }
            }
        }
        val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        val decoded = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: error("A.SYRIA panorama image could not be decoded")
        decoded.asImageBitmap()
    }

    val pageProgress by animateFloatAsState(
        targetValue = pageIndex.coerceIn(0, 4).toFloat(),
        animationSpec = tween(durationMillis = 860, easing = FastOutSlowInEasing),
        label = "ayria_panorama_page"
    )
    val horizontalParallax = roll.coerceIn(-20f, 20f) * 0.0035f
    val verticalParallax = pitch.coerceIn(-20f, 20f) * 0.0025f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(AyriaColors.abyss)
    ) {
        val cameraX = size.width * pageProgress - size.width * horizontalParallax
        val cameraY = size.height * verticalParallax
        drawImage(
            image = panoramaBitmap,
            dstOffset = androidx.compose.ui.unit.IntOffset(
                x = -cameraX.roundToInt(),
                y = cameraY.roundToInt()
            ),
            dstSize = androidx.compose.ui.unit.IntSize(
                width = (size.width * 5f).roundToInt(),
                height = size.height.roundToInt()
            ),
            filterQuality = androidx.compose.ui.graphics.FilterQuality.High
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x1A020918),
                    Color.Transparent,
                    Color(0x44020818)
                )
            )
        )
    }
}
