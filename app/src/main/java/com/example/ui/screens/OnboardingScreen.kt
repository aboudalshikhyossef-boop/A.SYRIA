package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import kotlinx.coroutines.launch

private val CyberAmber  = Color(0xFFFFAA00)
private val CyberGreen  = Color(0xFF00FF88)
private val CyberPurple = Color(0xFFBB66FF)

data class UserProfileData(val gender: String, val birthYear: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: (UserProfileData?) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // Profile state collected on page 3
    var selectedGender by remember { mutableStateOf("") }
    var selectedBirthYear by remember { mutableStateOf(1995) }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow"
    )

    val pageColors = listOf(CyberCyan, CyberAmber, CyberGreen, CyberPurple)
    val currentColor = pageColors.getOrElse(pagerState.currentPage) { CyberCyan }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF040D14))) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(currentColor.copy(alpha = 0.04f), Color.Transparent), radius = 900f)))

        Column(modifier = Modifier.fillMaxSize()) {
            // Skip
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {
                TextButton(onClick = { onFinish(null) }) {
                    Text("تخطى / SKIP", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> InfoPage(icon = Icons.Default.Security, iconTint = CyberCyan,
                        title = "SOVEREIGN INTELLIGENCE", titleAr = "سيادة الاستخبارات",
                        subtitleAr = "مستشارك الأمني المدعوم بالذكاء الاصطناعي. ابقَ محمياً ومتعلماً وروحانياً في مركز قيادة واحد.",
                        subtitle = "Your AI-powered cybersecurity advisor. Stay protected, educated, and spiritually grounded.",
                        features = listOf("🛡️ تنبيهات أمنية ذكية / AI Security Alerts", "🕌 مواقيت الصلاة / Prayer Times", "🎓 أكاديمية الأمن / Cyber Academy"),
                        accentColor = CyberCyan, glowAlpha = glowAlpha)
                    1 -> InfoPage(icon = Icons.Default.Lock, iconTint = CyberAmber,
                        title = "AI BRAIN REQUIRED", titleAr = "مفتاح الذكاء الاصطناعي",
                        subtitleAr = "فعّل العقل الاصطناعي بمفتاح Groq مجاني. يتيح لك الدردشة والتحليل والتخصيص.",
                        subtitle = "Get a free Groq API key from console.groq.com for unlimited AI chat and security analysis.",
                        features = listOf("1. console.groq.com للحصول على المفتاح", "2. أنشئ مفتاحاً مجانياً", "⚡ سريع جداً — LLaMA 3.3 70B"),
                        accentColor = CyberAmber, glowAlpha = glowAlpha)
                    2 -> InfoPage(icon = Icons.Default.School, iconTint = CyberGreen,
                        title = "PERSONALIZED FOR YOU", titleAr = "مخصص لك تماماً",
                        subtitleAr = "معالج المعايرة يُكيّف المحتوى حسب مستواك واهتماماتك وأهدافك الأمنية.",
                        subtitle = "Calibration wizard adapts all content to your skill level, interests, and security goals.",
                        features = listOf("📊 معايرة المستوى / Skill calibration", "🎯 تعلم هادف / Goal-based learning", "⚡ نقاط يومية / Daily XP & streaks"),
                        accentColor = CyberGreen, glowAlpha = glowAlpha)
                    3 -> ProfilePage(
                        selectedGender = selectedGender,
                        selectedBirthYear = selectedBirthYear,
                        onGenderSelect = { selectedGender = it },
                        onBirthYearChange = { selectedBirthYear = it },
                        accentColor = CyberPurple,
                        glowAlpha = glowAlpha
                    )
                }
            }

            // Bottom
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { idx ->
                        val isActive = pagerState.currentPage == idx
                        Box(modifier = Modifier.size(if (isActive) 10.dp else 6.dp).clip(CircleShape).background(if (isActive) pageColors[idx] else Color.White.copy(alpha = 0.2f)))
                    }
                }
                // Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            val profile = if (selectedGender.isNotBlank()) UserProfileData(selectedGender, selectedBirthYear) else null
                            onFinish(profile)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < 3) "التالي  ▶  NEXT" else "ابدأ الآن  ⚡  LAUNCH",
                        color = currentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfilePage(
    selectedGender: String,
    selectedBirthYear: Int,
    onGenderSelect: (String) -> Unit,
    onBirthYearChange: (Int) -> Unit,
    accentColor: Color,
    glowAlpha: Float
) {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        // Icon
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(100.dp).background(accentColor.copy(alpha = glowAlpha * 0.08f), CircleShape).border(1.dp, accentColor.copy(alpha = glowAlpha * 0.4f), CircleShape))
            Icon(Icons.Default.Person, null, tint = accentColor, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("NEURAL PROFILE", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, textAlign = TextAlign.Center)
        Text("الملف الشخصي", color = accentColor.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(8.dp))
        Text("يساعد الذكاء الاصطناعي في تخصيص المحتوى لك بشكل أفضل", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))

        // Gender selection
        Column(modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp)).background(accentColor.copy(alpha = 0.04f), RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الجنس / Gender", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderChip("ذكر / Male", selectedGender == "Male", accentColor, Modifier.weight(1f)) { onGenderSelect("Male") }
                GenderChip("أنثى / Female", selectedGender == "Female", accentColor, Modifier.weight(1f)) { onGenderSelect("Female") }
            }

            Spacer(Modifier.height(4.dp))
            Text("سنة الميلاد / Birth Year", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { if (selectedBirthYear > 1950) onBirthYearChange(selectedBirthYear - 1) },
                    modifier = Modifier.size(40.dp).background(accentColor.copy(alpha = 0.12f), CircleShape).border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape)) {
                    Text("◀", color = accentColor, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedBirthYear.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("العمر: ${currentYear - selectedBirthYear} سنة", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                }
                IconButton(onClick = { if (selectedBirthYear < currentYear - 10) onBirthYearChange(selectedBirthYear + 1) },
                    modifier = Modifier.size(40.dp).background(accentColor.copy(alpha = 0.12f), CircleShape).border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape)) {
                    Text("▶", color = accentColor, fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("يمكنك تخطي هذه الخطوة بالضغط على 'تخطى' في الأعلى", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun GenderChip(label: String, selected: Boolean, accentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(44.dp).border(1.dp, if (selected) accentColor else Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).background(if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(10.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) accentColor else Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InfoPage(icon: ImageVector, iconTint: Color, title: String, titleAr: String, subtitle: String, subtitleAr: String, features: List<String>, accentColor: Color, glowAlpha: Float) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(100.dp).background(accentColor.copy(alpha = glowAlpha * 0.08f), CircleShape).border(1.dp, accentColor.copy(alpha = glowAlpha * 0.4f), CircleShape))
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, textAlign = TextAlign.Center)
        Text(titleAr, color = accentColor.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(16.dp))
        Text(subtitleAr, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Column(modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)).background(accentColor.copy(alpha = 0.04f), RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            features.forEach { Text(it, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace) }
        }
    }
}
