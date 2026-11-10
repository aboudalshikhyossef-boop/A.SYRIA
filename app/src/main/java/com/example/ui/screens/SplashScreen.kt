package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaAtmosphere
import com.example.ui.brand.AyriaBrandMark
import com.example.ui.brand.AyriaColors
import com.example.ui.brand.AyriaVerseCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isArabic: Boolean = true,
    onNavigateToDashboard: () -> Unit
) {
    val currentCallback by rememberUpdatedState(onNavigateToDashboard)
    val markScale = remember { Animatable(0.88f) }
    val markAlpha = remember { Animatable(0f) }
    val copyAlpha = remember { Animatable(0f) }

    val orbitTransition = rememberInfiniteTransition(label = "splash_orbit")
    val orbitRotation by orbitTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_orbit_rotation"
    )
    val haloPulse by orbitTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_halo"
    )

    LaunchedEffect(Unit) {
        launch { markAlpha.animateTo(1f, animationSpec = tween(760, easing = FastOutSlowInEasing)) }
        launch { markScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 155f)) }
        delay(440)
        copyAlpha.animateTo(1f, animationSpec = tween(520, easing = FastOutSlowInEasing))
        delay(430)
        currentCallback()
    }

    AyriaAtmosphere(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(286.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * 0.43f
                    drawCircle(
                        color = AyriaColors.teal.copy(alpha = 0.08f * haloPulse),
                        radius = radius * 1.26f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.055f),
                        radius = radius * 1.08f,
                        center = center,
                        style = Stroke(width = 0.7.dp.toPx())
                    )
                    drawArc(
                        color = AyriaColors.glacier.copy(alpha = 0.34f),
                        startAngle = orbitRotation,
                        sweepAngle = 58f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = Stroke(width = 1.35.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = AyriaColors.ion.copy(alpha = 0.24f),
                        startAngle = 180f - orbitRotation * 0.42f,
                        sweepAngle = 34f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            center.x - radius * 0.83f,
                            center.y - radius * 0.83f
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 1.66f, radius * 1.66f),
                        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                AyriaBrandMark(
                    modifier = Modifier
                        .size(252.dp)
                        .graphicsLayer {
                            alpha = markAlpha.value
                            scaleX = markScale.value
                            scaleY = markScale.value
                        },
                    contentDescription = if (isArabic) "شعار A.SYRIA" else "A.SYRIA logo"
                )
            }

            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.alpha(copyAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "A.SYRIA",
                    color = AyriaColors.pearl,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.2.sp
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    text = if (isArabic) "سيادة رقمية. بثقة ووضوح." else "DIGITAL SOVEREIGNTY",
                    color = AyriaColors.glacier.copy(alpha = 0.82f),
                    fontSize = if (isArabic) 14.sp else 10.sp,
                    fontFamily = AyriaArabicFont,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = if (isArabic) 0.sp else 2.4.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                AyriaVerseCard(
                    isArabic = isArabic,
                    modifier = Modifier.width(318.dp)
                )
                Spacer(Modifier.height(22.dp))
                androidx.compose.material3.Text(
                    text = if (isArabic) "حماية  •  ذكاء  •  معرفة" else "PROTECTION  •  INTELLIGENCE  •  KNOWLEDGE",
                    color = AyriaColors.muted.copy(alpha = 0.72f),
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}