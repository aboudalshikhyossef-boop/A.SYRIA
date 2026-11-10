package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.brand.AyriaActionButton
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaAtmosphere
import com.example.ui.brand.AyriaBrandMark
import com.example.ui.brand.AyriaColors
import com.example.ui.brand.AyriaEyebrow
import com.example.ui.brand.AyriaGlassPanel
import kotlinx.coroutines.launch

data class UserProfileData(val gender: String, val birthYear: Int)

@Composable
fun OnboardingScreen(
    isArabic: Boolean = true,
    onFinish: (UserProfileData?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var selectedGender by remember { mutableStateOf("") }
    var selectedBirthYear by remember { mutableStateOf(1995) }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        AyriaAtmosphere(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "A.SYRIA",
                            color = AyriaColors.pearl,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.2.sp
                        )
                        Text(
                            text = if (isArabic) "منظومة السيادة الرقمية" else "DIGITAL SOVEREIGNTY",
                            color = AyriaColors.muted,
                            fontSize = 9.sp,
                            letterSpacing = if (isArabic) 0.sp else 1.3.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onFinish(null) }) {
                        Text(
                            text = if (isArabic) "تخطي" else "SKIP",
                            color = AyriaColors.muted,
                            fontSize = 12.sp,
                            fontFamily = AyriaArabicFont
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    pageSpacing = 14.dp
                ) { page ->
                    when (page) {
                        0 -> WelcomeHeroPage(isArabic = isArabic)
                        1 -> ProductPillarsPage(isArabic = isArabic)
                        2 -> PersonalizationIntroPage(isArabic = isArabic)
                        3 -> ProfilePage(
                            isArabic = isArabic,
                            selectedGender = selectedGender,
                            selectedBirthYear = selectedBirthYear,
                            onGenderSelect = { selectedGender = it },
                            onBirthYearChange = { selectedBirthYear = it }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(pagerState.currentPage + 1).toString().padStart(2, '0')} / 04",
                            color = AyriaColors.dim,
                            fontSize = 10.sp,
                            letterSpacing = 1.3.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(if (index == pagerState.currentPage) 26.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pagerState.currentPage) AyriaColors.teal
                                            else Color.White.copy(alpha = 0.17f)
                                        )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pagerState.currentPage > 0) {
                            AyriaActionButton(
                                text = if (isArabic) "رجوع" else "BACK",
                                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                modifier = Modifier.weight(0.78f).height(56.dp),
                                secondary = true
                            )
                        }
                        AyriaActionButton(
                            text = when {
                                pagerState.currentPage < 3 -> if (isArabic) "متابعة" else "CONTINUE"
                                else -> if (isArabic) "ابدأ التهيئة" else "SET UP A.SYRIA"
                            },
                            onClick = {
                                if (pagerState.currentPage < 3) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                } else {
                                    val profile = if (selectedGender.isNotBlank()) {
                                        UserProfileData(selectedGender, selectedBirthYear)
                                    } else null
                                    onFinish(profile)
                                }
                            },
                            modifier = Modifier.weight(1.45f).height(56.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeroPage(isArabic: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(
            text = if (isArabic) "منظومة رقمية متكاملة" else "ONE SOVEREIGN DIGITAL SPACE"
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.size(254.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.45f
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(AyriaColors.teal.copy(alpha = 0.20f), Color.Transparent),
                        center = center,
                        radius = radius * 1.35f
                    ),
                    radius = radius * 1.35f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
                drawArc(
                    color = AyriaColors.glacier.copy(alpha = 0.46f),
                    startAngle = 212f,
                    sweepAngle = 72f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3.dp.toPx())
                )
            }
            AyriaBrandMark(
                modifier = Modifier.size(224.dp),
                contentDescription = if (isArabic) "رمز A.SYRIA الزجاجي" else "A.SYRIA glass emblem"
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isArabic) "A.SYRIA" else "A.SYRIA",
            color = AyriaColors.pearl,
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.5.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isArabic) "سيادتك الرقمية، في تجربة واحدة." else "Your digital world. One intelligent space.",
            color = AyriaColors.glacier,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            textAlign = TextAlign.Center,
            lineHeight = 29.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isArabic)
                "حماية ذكية، معرفة منظمة، ومساعد رقمي يعمل معك — ضمن تجربة واحدة متناسقة."
            else
                "Intelligent protection, structured knowledge, and an AI companion — brought together in one coherent experience.",
            color = AyriaColors.muted,
            fontSize = 13.sp,
            fontFamily = AyriaArabicFont,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
    }
}

@Composable
private fun ProductPillarsPage(isArabic: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "أركان المنظومة" else "THE ECOSYSTEM")
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (isArabic) "أربع مساحات، بهوية واحدة." else "Four capabilities. One identity.",
            color = AyriaColors.pearl,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 35.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isArabic)
                "كل وظيفة لها دور واضح، وجميعها تعمل ضمن منظومة A.SYRIA."
            else
                "Every capability has a clear purpose, connected through the A.SYRIA experience.",
            color = AyriaColors.muted,
            fontSize = 13.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(22.dp))
        AyriaGlassPanel(modifier = Modifier.fillMaxWidth(), highlighted = true) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
                PillarLine(
                    icon = Icons.Rounded.Security,
                    title = if (isArabic) "الحماية الرقمية" else "Digital protection",
                    subtitle = if (isArabic) "أدوات أمنية وتحليل واعٍ للمخاطر." else "Security tools and informed risk analysis."
                )
                PillarLine(
                    icon = Icons.Rounded.AutoAwesome,
                    title = if (isArabic) "الذكاء الاصطناعي" else "Artificial intelligence",
                    subtitle = if (isArabic) "مساعد للتحليل والإجابة والتوجيه." else "An assistant for analysis, answers, and guidance."
                )
                PillarLine(
                    icon = Icons.Rounded.School,
                    title = if (isArabic) "التعلم والمعرفة" else "Learning and knowledge",
                    subtitle = if (isArabic) "أكاديمية ومكتبة وموارد منظمة." else "A structured academy, library, and resources."
                )
                PillarLine(
                    icon = Icons.Rounded.Lock,
                    title = if (isArabic) "الخصوصية والتحكم" else "Privacy and control",
                    subtitle = if (isArabic) "مساحة شخصية وإعدادات تحترم اختيارك." else "A personal space with settings you control."
                )
            }
        }
    }
}

@Composable
private fun PersonalizationIntroPage(isArabic: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "مصممة حولك" else "ADAPTS TO YOU")
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (isArabic) "ليست تجربة واحدة للجميع." else "A better experience starts with context.",
            color = AyriaColors.pearl,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 35.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isArabic)
                "سنطرح عليك ثلاثة أسئلة قصيرة عن مستواك واهتماماتك وهدفك، حتى تتكيف الأكاديمية والمحتوى مع ما تحتاجه."
            else
                "Three short questions about your experience, interests, and goal help tailor the academy and learning content to you.",
            color = AyriaColors.muted,
            fontSize = 13.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        AyriaGlassPanel(modifier = Modifier.fillMaxWidth(), highlighted = true) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SetupStep(number = "01", title = if (isArabic) "مستواك الحالي" else "Your level")
                SetupStep(number = "02", title = if (isArabic) "المجالات التي تهمك" else "Your interests")
                SetupStep(number = "03", title = if (isArabic) "هدفك الأساسي" else "Your primary goal")
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (isArabic)
                "يمكنك تعديل تفضيلاتك لاحقًا. ومعلومات الملف الشخصي في الخطوة التالية اختيارية."
            else
                "You can update your preferences later. The profile details on the next page are optional.",
            color = AyriaColors.dim,
            fontSize = 11.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isArabic)
                "بعض ميزات الذكاء الاصطناعي تحتاج إلى مفتاح Gemini، ويمكن إضافته من الإعدادات بعد إكمال التهيئة."
            else
                "Some AI features require a Gemini API key, which can be added in Settings after setup.",
            color = AyriaColors.dim,
            fontSize = 10.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun PillarLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(AyriaColors.teal.copy(alpha = 0.10f))
                .border(1.dp, AyriaColors.teal.copy(alpha = 0.20f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AyriaColors.glacier, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = AyriaColors.pearl, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AyriaArabicFont)
            Text(subtitle, color = AyriaColors.muted, fontSize = 11.sp, fontFamily = AyriaArabicFont, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun SetupStep(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = number,
            color = AyriaColors.teal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(14.dp))
        Text(title, color = AyriaColors.pearl, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = AyriaArabicFont)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = AyriaColors.dim, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun ProfilePage(
    isArabic: Boolean,
    selectedGender: String,
    selectedBirthYear: Int,
    onGenderSelect: (String) -> Unit,
    onBirthYearChange: (Int) -> Unit
) {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "ملف اختياري" else "OPTIONAL PROFILE")
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isArabic) "لنجعل تجربتك أقرب إليك." else "A little context can make the experience more personal.",
            color = AyriaColors.pearl,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 34.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isArabic)
                "هذه المعلومات اختيارية، ويمكنك تخطيها أو تعديلها لاحقًا."
            else
                "These details are optional. You can skip them or change them later.",
            color = AyriaColors.muted,
            fontSize = 12.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(22.dp))
        AyriaGlassPanel(modifier = Modifier.fillMaxWidth(), highlighted = true) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (isArabic) "كيف تفضّل أن نخاطبك؟" else "How should we address you?",
                    color = AyriaColors.glacier,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AyriaArabicFont
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileChoice(
                        text = if (isArabic) "ذكر" else "Male",
                        selected = selectedGender == "Male",
                        modifier = Modifier.weight(1f),
                        onClick = { onGenderSelect("Male") }
                    )
                    ProfileChoice(
                        text = if (isArabic) "أنثى" else "Female",
                        selected = selectedGender == "Female",
                        modifier = Modifier.weight(1f),
                        onClick = { onGenderSelect("Female") }
                    )
                }
                androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = AyriaColors.teal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "سنة الميلاد" else "Birth year",
                        color = AyriaColors.glacier,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AyriaArabicFont
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = selectedBirthYear.toString(),
                        color = AyriaColors.pearl,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    ProfileChoice(
                        text = "−",
                        selected = false,
                        modifier = Modifier.size(48.dp),
                        onClick = { if (selectedBirthYear > 1950) onBirthYearChange(selectedBirthYear - 1) }
                    )
                    Spacer(Modifier.width(18.dp))
                    Text(
                        text = if (isArabic) "العمر التقريبي: ${currentYear - selectedBirthYear}" else "Approx. age: ${currentYear - selectedBirthYear}",
                        color = AyriaColors.muted,
                        fontSize = 11.sp,
                        fontFamily = AyriaArabicFont
                    )
                    Spacer(Modifier.width(18.dp))
                    ProfileChoice(
                        text = "+",
                        selected = false,
                        modifier = Modifier.size(48.dp),
                        onClick = { if (selectedBirthYear < currentYear - 10) onBirthYearChange(selectedBirthYear + 1) }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isArabic) "يمكنك المتابعة دون اختيار أي معلومة." else "You can continue without selecting any profile details.",
            color = AyriaColors.dim,
            fontSize = 10.sp,
            fontFamily = AyriaArabicFont
        )
    }
}

@Composable
private fun ProfileChoice(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) AyriaColors.teal.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.045f))
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) AyriaColors.teal.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.13f)
                ),
                shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) AyriaColors.glacier else AyriaColors.muted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = AyriaArabicFont
        )
    }
}