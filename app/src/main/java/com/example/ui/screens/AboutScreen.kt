package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import kotlin.math.sin

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val timeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Parallax Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = (timeOffset / 100f) * (Math.PI.toFloat() * 2f)
            val spacing = 30.dp.toPx()
            val offsetX = (sin(t * 0.5f) * 20f)
            val offsetY = (sin(t * 0.3f) * 20f)

            for (x in 0..(size.width / spacing).toInt() + 1) {
                drawLine(
                    color = CyberCyan.copy(alpha = 0.05f),
                    start = Offset(x * spacing + offsetX, 0f),
                    end = Offset(x * spacing + offsetX, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / spacing).toInt() + 1) {
                drawLine(
                    color = CyberCyan.copy(alpha = 0.05f),
                    start = Offset(0f, y * spacing + offsetY),
                    end = Offset(size.width, y * spacing + offsetY),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OPERATOR_MANUAL",
                        color = CyberCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SYSTEM CLASSIFICATION: TOP SECRET",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "[ EXIT ]",
                    color = Color.Red.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onBack() }.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Central Blueprint Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "A.SYRIA V4",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 6.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = CyberCyan.copy(alpha = 0.8f * pulseAlpha),
                                blurRadius = 20f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SOVEREIGN OS",
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(60.dp))

                    Text(
                        text = "A bridge between Cyber-Intelligence\nand Spiritual Wisdom.",
                        color = AmberZen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(60.dp))

                    Text(
                        text = "Designed & Engineered by",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ABOUDA.AL.SHEKH.YOSSEF",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        modifier = Modifier
                            .border(0.5.dp, CyberCyan.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "v4.0.0 Stable Build",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "ENCRYPTION: QUANTUM_SECURE // ARCHITECTURE: NEURAL_MESH",
                color = CyberCyan.copy(alpha = 0.3f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
