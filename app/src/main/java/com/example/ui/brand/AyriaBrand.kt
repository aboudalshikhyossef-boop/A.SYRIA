package com.example.ui.brand

import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.matchParentSize
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
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.asyria.v4.R

internal object AyriaColors {
    val abyss = Color(0xFF06141C)
    val deep = Color(0xFF091D28)
    val ocean = Color(0xFF102B38)
    val panel = Color(0xFF122D39)
    val teal = Color(0xFF72D5D0)
    val glacier = Color(0xFFA9ECE5)
    val ion = Color(0xFF7FA9F0)
    val pearl = Color(0xFFEAF3F1)
    val muted = Color(0xFF9AB1B7)
    val dim = Color(0xFF6F8992)
    val line = Color(0x33D9F5F2)
    val glass = Color(0x12D8F7F4)
}

internal val AyriaArabicFont = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_bold, FontWeight.Bold)
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
                        Color(0xFF081923),
                        AyriaColors.abyss,
                        Color(0xFF071A26)
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
                        AyriaColors.teal.copy(alpha = 0.20f),
                        Color(0xFF1A7180).copy(alpha = 0.075f),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = size.maxDimension * 0.66f
                ),
                radius = size.maxDimension * 0.66f,
                center = glowCenter
            )

            val lowerGlow = Offset(size.width * 0.92f, size.height * 0.78f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AyriaColors.ion.copy(alpha = 0.10f),
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
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.055f),
                AyriaColors.teal.copy(alpha = 0.11f)
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
            .border(width = 1.dp, brush = edge, shape = shape),
        content = content
    )
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