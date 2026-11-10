package com.example.ui.brand

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.max
import kotlin.math.cos
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.animation.core.LinearEasing
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
 * Procedural chrome-glacier world shared by all five main destinations.
 * A subdued faceted field creates depth while a rounded metallic triangle
 * supplies the signature hollow chrome form from the visual reference.
 */
private const val GLACIER_MESH_COLS = 30
private const val GLACIER_MESH_ROWS = 13
private const val GLACIER_X_MIN = -0.10f
private const val GLACIER_X_MAX = 5.10f
private const val GLACIER_Y_MIN = -0.08f
private const val GLACIER_Y_MAX = 1.08f
private const val GLACIER_RELIEF = 0.60f
private const val GLACIER_SCREEN_ASPECT = 2.10f

private data class GlacierFacet(
    val x0: Float, val y0: Float,
    val x1: Float, val y1: Float,
    val x2: Float, val y2: Float,
    val base: Color,
    val glint: Float,
    val phase: Float
) {
    val minX: Float = minOf(x0, x1, x2)
    val maxX: Float = maxOf(x0, x1, x2)
}

private val glacierStops by lazy {
    listOf(
        0.00f to AyriaColors.abyss,
        0.15f to AyriaColors.deep,
        0.30f to AyriaColors.ocean,
        0.50f to AyriaColors.panel,
        0.70f to AyriaColors.ion,
        0.86f to AyriaColors.teal,
        1.00f to AyriaColors.glacier
    )
}

private fun glacierColor(value: Float): Color {
    val t = value.coerceIn(0f, 1f)
    for (index in 1 until glacierStops.size) {
        val (rightStop, rightColor) = glacierStops[index]
        if (t <= rightStop) {
            val (leftStop, leftColor) = glacierStops[index - 1]
            val fraction = ((t - leftStop) / (rightStop - leftStop)).coerceIn(0f, 1f)
            return lerp(leftColor, rightColor, fraction)
        }
    }
    return glacierStops.last().second
}

private fun glacierHeight(x: Float, y: Float): Float =
    0.55f * sin(x * 1.7f + 0.8f) * cos(y * 3.1f) +
        0.35f * sin(x * 3.9f + y * 2.3f) +
        0.20f * cos(x * 7.1f - y * 5.0f)

private fun buildGlacierMesh(seed: Long = 0x5A71AL): List<GlacierFacet> {
    val random = java.util.Random(seed)
    val stride = GLACIER_MESH_COLS + 1
    val vertexCount = stride * (GLACIER_MESH_ROWS + 1)
    val vx = FloatArray(vertexCount)
    val vy = FloatArray(vertexCount)
    val vz = FloatArray(vertexCount)

    for (row in 0..GLACIER_MESH_ROWS) {
        for (column in 0..GLACIER_MESH_COLS) {
            val index = row * stride + column
            val jitterX = if (column == 0 || column == GLACIER_MESH_COLS) 0f
                else (random.nextFloat() - 0.5f) * 0.72f
            val jitterY = if (row == 0 || row == GLACIER_MESH_ROWS) 0f
                else (random.nextFloat() - 0.5f) * 0.72f
            vx[index] = GLACIER_X_MIN +
                (column + jitterX) / GLACIER_MESH_COLS * (GLACIER_X_MAX - GLACIER_X_MIN)
            vy[index] = GLACIER_Y_MIN +
                (row + jitterY) / GLACIER_MESH_ROWS * (GLACIER_Y_MAX - GLACIER_Y_MIN)
            vz[index] = glacierHeight(vx[index], vy[index]) +
                (random.nextFloat() - 0.5f) * 0.24f
        }
    }

    val lightRaw = floatArrayOf(-0.45f, -0.55f, 0.70f)
    val lightLength = sqrt(lightRaw.sumOf { (it * it).toDouble() }.toFloat())
    val light = FloatArray(3) { lightRaw[it] / lightLength }
    val halfRaw = floatArrayOf(light[0], light[1], light[2] + 1f)
    val halfLength = sqrt(halfRaw.sumOf { (it * it).toDouble() }.toFloat())
    val halfVector = FloatArray(3) { halfRaw[it] / halfLength }

    val facets = ArrayList<GlacierFacet>(GLACIER_MESH_COLS * GLACIER_MESH_ROWS * 2)

    fun addFacet(a: Int, b: Int, c: Int) {
        val ax = vx[a]
        val ay = vy[a] * GLACIER_SCREEN_ASPECT
        val az = vz[a] * GLACIER_RELIEF
        val e1x = vx[b] - ax
        val e1y = vy[b] * GLACIER_SCREEN_ASPECT - ay
        val e1z = vz[b] * GLACIER_RELIEF - az
        val e2x = vx[c] - ax
        val e2y = vy[c] * GLACIER_SCREEN_ASPECT - ay
        val e2z = vz[c] * GLACIER_RELIEF - az

        var nx = e1y * e2z - e1z * e2y
        var ny = e1z * e2x - e1x * e2z
        var nz = e1x * e2y - e1y * e2x
        if (nz < 0f) {
            nx = -nx
            ny = -ny
            nz = -nz
        }
        val normalLength = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-6f)
        nx /= normalLength
        ny /= normalLength
        nz /= normalLength

        val diffuse = max(0f, nx * light[0] + ny * light[1] + nz * light[2])
        val specular = max(0f, nx * halfVector[0] + ny * halfVector[1] + nz * halfVector[2]).pow(30f)
        val shade = (diffuse.pow(1.6f) * 0.75f + specular * 0.35f +
            (random.nextFloat() - 0.5f) * 0.05f).coerceIn(0f, 1f)

        facets += GlacierFacet(
            x0 = vx[a], y0 = vy[a],
            x1 = vx[b], y1 = vy[b],
            x2 = vx[c], y2 = vy[c],
            base = glacierColor(shade),
            glint = specular.coerceIn(0f, 1f),
            phase = random.nextFloat() * 6.2832f
        )
    }

    for (row in 0 until GLACIER_MESH_ROWS) {
        for (column in 0 until GLACIER_MESH_COLS) {
            val v00 = row * stride + column
            val v10 = v00 + 1
            val v01 = v00 + stride
            val v11 = v01 + 1
            if ((column + row) % 2 == 0) {
                addFacet(v00, v10, v11)
                addFacet(v00, v11, v01)
            } else {
                addFacet(v00, v10, v01)
                addFacet(v10, v11, v01)
            }
        }
    }
    return facets
}

private fun buildChromeTrianglePath(
    width: Float,
    height: Float,
    centerX: Float,
    topY: Float,
    halfWidth: Float,
    baseY: Float,
    cameraX: Float,
    cameraY: Float
): Path {
    val path = Path()
    val curveHeight = baseY - topY
    fun x(value: Float) = value - cameraX
    fun y(value: Float) = value + cameraY

    path.moveTo(x(centerX), y(topY))
    path.cubicTo(
        x(centerX + halfWidth * 0.09f), y(topY - curveHeight * 0.015f),
        x(centerX + halfWidth * 0.14f), y(topY + curveHeight * 0.10f),
        x(centerX + halfWidth * 0.24f), y(topY + curveHeight * 0.29f)
    )
    path.lineTo(x(centerX + halfWidth * 0.91f), y(baseY - curveHeight * 0.12f))
    path.cubicTo(
        x(centerX + halfWidth * 1.04f), y(baseY + curveHeight * 0.07f),
        x(centerX + halfWidth * 0.88f), y(baseY + curveHeight * 0.10f),
        x(centerX + halfWidth * 0.70f), y(baseY + curveHeight * 0.10f)
    )
    path.lineTo(x(centerX - halfWidth * 0.70f), y(baseY + curveHeight * 0.10f))
    path.cubicTo(
        x(centerX - halfWidth * 0.88f), y(baseY + curveHeight * 0.10f),
        x(centerX - halfWidth * 1.04f), y(baseY + curveHeight * 0.07f),
        x(centerX - halfWidth * 0.91f), y(baseY - curveHeight * 0.12f)
    )
    path.lineTo(x(centerX - halfWidth * 0.24f), y(topY + curveHeight * 0.29f))
    path.cubicTo(
        x(centerX - halfWidth * 0.14f), y(topY + curveHeight * 0.10f),
        x(centerX - halfWidth * 0.09f), y(topY - curveHeight * 0.015f),
        x(centerX), y(topY)
    )
    path.close()
    return path
}

private fun DrawScope.drawChromeRim(
    path: Path,
    thickness: Float,
    alpha: Float,
    shimmer: Float,
    topY: Float,
    bottomY: Float
) {
    val rounded = Stroke(
        width = thickness,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    val outer = Stroke(
        width = thickness * 1.34f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    val inner = Stroke(
        width = thickness * 0.60f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    val chrome = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF07122E),
            0.10f to Color(0xFFEAFBFF),
            0.20f to Color(0xFF5476BB),
            0.34f to Color(0xFF02071C),
            0.47f to Color(0xFFBFEFFF),
            0.55f to Color(0xFFFFFFFF),
            0.68f to Color(0xFF101D52),
            0.82f to Color(0xFF020616),
            0.94f to Color(0xFF8EEBFF),
            1.00f to Color(0xFF172344)
        ),
        start = Offset(0f, topY),
        end = Offset(0f, bottomY)
    )
    val innerChrome = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF03071A),
            0.28f to Color(0xFF182D70),
            0.48f to Color(0xFF020719),
            0.70f to Color(0xFF5BC8F8),
            1.00f to Color(0xFF020A24)
        ),
        start = Offset(0f, topY),
        end = Offset(0f, bottomY)
    )

    drawPath(
        path = path,
        color = Color(0xFF009DFF),
        alpha = alpha * (0.20f + shimmer * 0.035f),
        style = Stroke(width = thickness * 1.90f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = path,
        color = Color(0xFF010513),
        alpha = alpha,
        style = outer
    )
    drawPath(path = path, brush = chrome, alpha = alpha, style = rounded)
    drawPath(path = path, brush = innerChrome, alpha = alpha * 0.90f, style = inner)
    drawPath(
        path = path,
        color = Color.White,
        alpha = alpha * (0.30f + shimmer * 0.12f),
        style = Stroke(
            width = thickness * 0.105f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    drawPath(
        path = path,
        color = Color(0xFF54DFFF),
        alpha = alpha * (0.18f + shimmer * 0.08f),
        style = Stroke(
            width = max(1.1.dp.toPx(), thickness * 0.035f),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Procedural chrome-glacier background, shared continuously by all five main tabs.
 * It uses vector-like Canvas geometry instead of a scaled bitmap, preserving crisp
 * edges on any screen density while retaining a unified horizontal pan and gyroscope
 * parallax. The optional scrim keeps cards and Arabic copy readable.
 */
@Composable
internal fun AyriaPanoramaBackdrop(
    pageIndex: Int,
    roll: Float = 0f,
    pitch: Float = 0f,
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.34f
) {
    val facets = remember { buildGlacierMesh() }
    val shimmer by rememberInfiniteTransition(label = "ayria_chrome_glint").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ayria_glint_phase"
    )
    val pageProgress by animateFloatAsState(
        targetValue = pageIndex.coerceIn(0, 4).toFloat(),
        animationSpec = tween(durationMillis = 860, easing = FastOutSlowInEasing),
        label = "ayria_panorama_page"
    )
    val facetPath = remember { Path() }
    val chromePath = remember { Path() }
    val leftReflectionPath = remember { Path() }
    val rightReflectionPath = remember { Path() }
    val horizontalParallax = roll.coerceIn(-20f, 20f) * 0.0035f
    val verticalParallax = pitch.coerceIn(-20f, 20f) * 0.0025f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(AyriaColors.abyss)
    ) {
        val width = size.width
        val height = size.height
        val worldWidth = width * 5f
        val cameraX = width * pageProgress - width * horizontalParallax
        val cameraY = height * verticalParallax
        val visibleMin = cameraX / width - 0.08f
        val visibleMax = (cameraX + width) / width + 0.08f

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF060B1A),
                    Color(0xFF0C1934),
                    Color(0xFF07152F),
                    Color(0xFF101F42),
                    Color(0xFF050B1B)
                ),
                start = Offset(-cameraX, 0f),
                end = Offset(worldWidth - cameraX, height)
            )
        )

        for (page in 0..4) {
            val center = Offset(
                x = width * (page + 0.50f) - cameraX,
                y = height * (0.20f + (page % 2) * 0.36f) + cameraY
            )
            val radius = width * 0.82f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x553D79D8),
                        Color(0x282B6CB8),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Faceted dark-blue glass field. Only triangles near the current viewport are drawn.
        for (facet in facets) {
            if (facet.maxX < visibleMin || facet.minX > visibleMax) continue

            facetPath.rewind()
            facetPath.moveTo(facet.x0 * width - cameraX, facet.y0 * height + cameraY)
            facetPath.lineTo(facet.x1 * width - cameraX, facet.y1 * height + cameraY)
            facetPath.lineTo(facet.x2 * width - cameraX, facet.y2 * height + cameraY)
            facetPath.close()

            val pulse = 0.5f + 0.5f * sin(shimmer * 6.2832f + facet.phase)
            val fill = lerp(facet.base, AyriaColors.glacier, facet.glint * pulse * 0.30f)
            drawPath(path = facetPath, color = fill, alpha = 0.34f)
            if (facet.glint > 0.52f) {
                drawPath(
                    path = facetPath,
                    color = AyriaColors.glacier,
                    alpha = 0.035f + facet.glint * pulse * 0.065f,
                    style = Stroke(width = 0.65.dp.toPx())
                )
            }
        }

        // Lower the faceted field before drawing the bright chrome ribbon.
        drawRect(color = AyriaColors.abyss, alpha = scrimAlpha.coerceIn(0f, 0.72f))

        // Subtle outer structures at the two ends echo the side reflections in the reference.
        leftReflectionPath.rewind()
        val leftPath = buildChromeTrianglePath(
            width = width,
            height = height,
            centerX = width * 0.46f,
            topY = height * 0.20f,
            halfWidth = width * 0.67f,
            baseY = height * 0.82f,
            cameraX = cameraX,
            cameraY = cameraY
        )
        leftReflectionPath.addPath(leftPath)
        drawChromeRim(
            path = leftReflectionPath,
            thickness = height * 0.047f,
            alpha = 0.25f,
            shimmer = shimmer,
            topY = height * 0.20f,
            bottomY = height * 0.95f
        )

        rightReflectionPath.rewind()
        val rightPath = buildChromeTrianglePath(
            width = width,
            height = height,
            centerX = width * 4.54f,
            topY = height * 0.20f,
            halfWidth = width * 0.67f,
            baseY = height * 0.82f,
            cameraX = cameraX,
            cameraY = cameraY
        )
        rightReflectionPath.addPath(rightPath)
        drawChromeRim(
            path = rightReflectionPath,
            thickness = height * 0.047f,
            alpha = 0.22f,
            shimmer = shimmer,
            topY = height * 0.20f,
            bottomY = height * 0.95f
        )

        // One uninterrupted rounded triangular chrome band spans the five-page world.
        chromePath.rewind()
        val mainPath = buildChromeTrianglePath(
            width = width,
            height = height,
            centerX = width * 2.50f,
            topY = -height * 0.06f,
            halfWidth = width * 2.39f,
            baseY = height * 0.93f,
            cameraX = cameraX,
            cameraY = cameraY
        )
        chromePath.addPath(mainPath)
        drawChromeRim(
            path = chromePath,
            thickness = height * 0.074f,
            alpha = 0.92f,
            shimmer = shimmer,
            topY = -height * 0.06f,
            bottomY = height * 1.04f
        )

        // Soft vignette ties the five sections together without hiding the chrome.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x26010918),
                    Color.Transparent,
                    Color(0x5C010817)
                )
            )
        )
    }
}
