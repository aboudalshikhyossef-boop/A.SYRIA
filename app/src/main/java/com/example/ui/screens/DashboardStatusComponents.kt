package com.example.ui.screens
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack

/** Stateless presentation components shared by the dashboard's status and metrics sections. */

@Composable
fun StatusHeaderCell(
    roll: Float, 
    pitch: Float, 
    operatorName: String, 
    neuralRole: String, 
    isStealth: Boolean
) {
    Column(
        modifier = Modifier
            .graphicsLayer {
                rotationX = -pitch * 0.4f
                rotationY = roll * 0.4f
                translationX = roll * 0.3f
                translationY = pitch * 0.3f
            }
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "A.SYRIA V5",
                color = Color.White,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = CyberCyan.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = 15f
                    )
                ),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 6.sp
            )
            
            if (isStealth) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Stealth Active",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = operatorName.uppercase(),
            color = AmberZen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        
        Text(
            text = neuralRole.uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "الحالة: آمن",
            color = CyberCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}

@Composable
fun PrayerItem(name: String, time: String, isAr: Boolean, isNext: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Text(
            text = name,
            color = if (isNext) AmberZen else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = time.replace(" AM", "").replace(" PM", "").replace(" ص", "").replace(" م", ""),
            color = if (isNext) CyberCyan else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
        )
        if (isNext) {
            Box(modifier = Modifier.height(2.dp).width(20.dp).background(AmberZen, RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
fun IntelligenceTicker(text: String, isOffline: Boolean = false, roll: Float, pitch: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = -pitch * 0.3f
                rotationY = roll * 0.3f
                translationX = roll * 0.2f
                translationY = pitch * 0.2f
            }
            .border(
                0.5.dp, 
                if (isOffline) Color.Red.copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.25f), 
                RoundedCornerShape(6.dp)
            )
            .background(
                if (isOffline) Color.Red.copy(alpha = 0.08f) else VoidBlack.copy(alpha = 0.6f), 
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isOffline) Color.Red else CyberCyan, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (isOffline) Color.Red else CyberCyan,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
        }
    }
}

@Composable
fun CyberRankMetricBar(
    score: Int,
    rank: String,
    progress: Float,
    roll: Float,
    pitch: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .graphicsLayer {
                rotationX = -pitch * 0.4f
                rotationY = roll * 0.4f
            }
            .border(
                width = 0.5.dp,
                color = CyberCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "رتبة الأمان: [${rank.uppercase()}]",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = CyberCyan.copy(alpha = 0.6f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        )
                    )
                )
                Text(
                    text = "مسار التدريب الأمني",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "$score PTS",
                color = AmberZen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Glowing Progress Bar Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CyberCyan, CyberCyan.copy(alpha = 0.4f))
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun ConnectionStatusPill(
    isOnline: Boolean,
    accent: Color,
    isAr: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "connPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAnim by infinite.animateFloat(
        initialValue = 0.10f, targetValue = 0.28f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .wrapContentSize()
            .border(0.75.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(50))
            .background(accent.copy(alpha = glowAnim), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(accent.copy(alpha = pulse), RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOnline) {
                if (isAr) "متصل بالشبكة" else "NETWORK ONLINE"
            } else {
                if (isAr) "غير متصل" else "NETWORK OFFLINE"
            },
            color = accent,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = accent.copy(alpha = 0.6f),
                    blurRadius = 10f
                )
            )
        )
    }
}
