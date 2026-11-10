package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.brand.AyriaActionButton
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaAtmosphere
import com.example.ui.brand.AyriaColors
import com.example.ui.brand.AyriaEyebrow
import com.example.ui.brand.AyriaGlassPanel
import com.example.ui.viewmodel.DashboardViewModel

private data class InterestOpt(
    val key: String,
    val ar: String,
    val en: String,
    val icon: ImageVector
)

private data class GoalOpt(
    val key: String,
    val ar: String,
    val en: String,
    val icon: ImageVector
)

@Composable
fun CalibrationScreen(
    viewModel: DashboardViewModel,
    onCalibrationComplete: () -> Unit
) {
    val isAr by viewModel.isArabic.collectAsState()
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    val levels = listOf(
        Triple("Beginner", if (isAr) "مبتدئ" else "Beginner", Icons.Rounded.Shield),
        Triple("Intermediate", if (isAr) "متوسط" else "Intermediate", Icons.Rounded.Security),
        Triple("Advanced", if (isAr) "متقدم" else "Advanced", Icons.Rounded.Code)
    )
    val levelDescriptions = mapOf(
        "Beginner" to if (isAr) "أتعرف إلى أساسيات الأمن الرقمي." else "I am learning the fundamentals.",
        "Intermediate" to if (isAr) "لدي معرفة أساسية وأريد التعمق." else "I know the basics and want to go deeper.",
        "Advanced" to if (isAr) "لدي خبرة وأبحث عن محتوى متقدم." else "I have experience and want advanced content."
    )

    val interestOpts = listOf(
        InterestOpt("Account Security", "أمن الحسابات", "Account security", Icons.Rounded.Lock),
        InterestOpt("Network Security", "أمن الشبكات", "Network security", Icons.Rounded.Language),
        InterestOpt("Phishing & Malware", "التصيد والبرمجيات الخبيثة", "Phishing & malware", Icons.Rounded.BugReport),
        InterestOpt("Ethical Hacking", "الاختبار الأمني الأخلاقي", "Ethical hacking", Icons.Rounded.Code),
        InterestOpt("Digital Privacy", "الخصوصية الرقمية", "Digital privacy", Icons.Rounded.Shield),
        InterestOpt("Mobile Security", "أمن الهواتف", "Mobile security", Icons.Rounded.PhoneAndroid),
        InterestOpt("Social Engineering", "الهندسة الاجتماعية", "Social engineering", Icons.Rounded.Groups),
        InterestOpt("Cryptography", "التشفير", "Cryptography", Icons.Rounded.Key),
        InterestOpt("Threat Intelligence", "استخبارات التهديدات", "Threat intelligence", Icons.Rounded.Search),
        InterestOpt("Incident Response", "الاستجابة للحوادث", "Incident response", Icons.Rounded.AutoAwesome)
    )

    val goalOpts = listOf(
        GoalOpt("self_protect", "حماية نفسي وعائلتي", "Protect myself and my family", Icons.Rounded.Home),
        GoalOpt("career", "الدخول إلى مجال الأمن السيبراني", "Build a cybersecurity career", Icons.Rounded.Work),
        GoalOpt("certification", "الاستعداد لشهادة احترافية", "Prepare for a certification", Icons.Rounded.WorkspacePremium),
        GoalOpt("threat_hunting", "تعلّم صيد التهديدات", "Learn threat hunting", Icons.Rounded.Search),
        GoalOpt("general", "توسيع معرفتي الرقمية", "Improve my digital knowledge", Icons.Rounded.MenuBook)
    )

    var selectedLevelKey by remember { mutableStateOf("Beginner") }
    val selectedInterests = remember { mutableStateListOf<String>() }
    var selectedGoal by remember { mutableStateOf("") }

    AyriaAtmosphere(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 18.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "A.SYRIA  /  PERSONAL SETUP",
                        color = AyriaColors.teal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        text = if (isAr) "لنهيّئ تجربتك" else "Let's shape your experience",
                        color = AyriaColors.pearl,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AyriaArabicFont
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(step + 1).toString().padStart(2, '0')} / 03",
                    color = AyriaColors.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = (step + 1f) / totalSteps,
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                color = AyriaColors.teal,
                trackColor = Color.White.copy(alpha = 0.10f)
            )
            Spacer(Modifier.height(18.dp))

            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { it / 5 },
                        animationSpec = tween(320)
                    ) + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { -it / 7 },
                            animationSpec = tween(210)
                        ) + fadeOut(tween(180)))
                },
                label = "calibration_steps"
            ) { activeStep ->
                when (activeStep) {
                    0 -> LevelStep(
                        isArabic = isAr,
                        levels = levels,
                        descriptions = levelDescriptions,
                        selectedKey = selectedLevelKey,
                        onSelect = { selectedLevelKey = it }
                    )
                    1 -> InterestsStep(
                        isArabic = isAr,
                        options = interestOpts,
                        selected = selectedInterests
                    )
                    else -> GoalStep(
                        isArabic = isAr,
                        options = goalOpts,
                        selectedKey = selectedGoal,
                        onSelect = { selectedGoal = it }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            val canProceed = when (step) {
                0 -> true
                1 -> selectedInterests.isNotEmpty()
                2 -> selectedGoal.isNotBlank()
                else -> false
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 0) {
                    AyriaActionButton(
                        text = if (isAr) "رجوع" else "BACK",
                        onClick = { step-- },
                        modifier = Modifier.weight(0.85f).height(56.dp),
                        secondary = true
                    )
                }
                AyriaActionButton(
                    text = if (step == totalSteps - 1) {
                        if (isAr) "ابدأ التجربة" else "ENTER A.SYRIA"
                    } else {
                        if (isAr) "التالي" else "CONTINUE"
                    },
                    onClick = {
                        if (step == totalSteps - 1) {
                            viewModel.saveCalibrationData(
                                selectedLevelKey,
                                selectedInterests.toSet(),
                                selectedGoal
                            )
                            onCalibrationComplete()
                        } else {
                            step++
                        }
                    },
                    modifier = Modifier.weight(if (step > 0) 1.4f else 1f).height(56.dp),
                    enabled = canProceed
                )
            }
        }
    }
}

@Composable
private fun LevelStep(
    isArabic: Boolean,
    levels: List<Triple<String, String, ImageVector>>,
    descriptions: Map<String, String>,
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "01  /  مستوى الخبرة" else "01  /  EXPERIENCE")
        Spacer(Modifier.height(15.dp))
        Text(
            text = if (isArabic) "من أين تبدأ؟" else "Where are you starting from?",
            color = AyriaColors.pearl,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isArabic)
                "اختر المستوى الأقرب إليك. لا توجد إجابة صحيحة أو خاطئة."
            else
                "Choose the level that feels closest. There is no right or wrong answer.",
            color = AyriaColors.muted,
            fontSize = 12.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(22.dp))
        levels.forEach { (key, label, icon) ->
            val selected = selectedKey == key
            SelectionRow(
                icon = icon,
                title = label,
                subtitle = descriptions[key].orEmpty(),
                selected = selected,
                onClick = { onSelect(key) }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun InterestsStep(
    isArabic: Boolean,
    options: List<InterestOpt>,
    selected: MutableList<String>
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "02  /  مجالات الاهتمام" else "02  /  INTERESTS")
        Spacer(Modifier.height(15.dp))
        Text(
            text = if (isArabic) "ما الذي يهمك أكثر؟" else "What interests you most?",
            color = AyriaColors.pearl,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isArabic) "اختر حتى ثلاثة مجالات" else "Choose up to three areas",
                color = AyriaColors.muted,
                fontSize = 12.sp,
                fontFamily = AyriaArabicFont
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${selected.size} / 3",
                color = AyriaColors.glacier,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(18.dp))
        options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                row.forEach { option ->
                    val active = selected.contains(option.key)
                    InterestTile(
                        option = option,
                        isArabic = isArabic,
                        selected = active,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (active) {
                                selected.remove(option.key)
                            } else if (selected.size < 3) {
                                selected.add(option.key)
                            }
                        }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun GoalStep(
    isArabic: Boolean,
    options: List<GoalOpt>,
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        AyriaEyebrow(text = if (isArabic) "03  /  الهدف الرئيسي" else "03  /  YOUR GOAL")
        Spacer(Modifier.height(15.dp))
        Text(
            text = if (isArabic) "ما الذي تريد تحقيقه؟" else "What would you like to achieve?",
            color = AyriaColors.pearl,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AyriaArabicFont,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isArabic)
                "سنستخدم اختيارك لترتيب المحتوى والتوصيات التعليمية."
            else
                "Your choice helps shape learning content and recommendations.",
            color = AyriaColors.muted,
            fontSize = 12.sp,
            fontFamily = AyriaArabicFont,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))
        options.forEach { option ->
            val active = selectedKey == option.key
            SelectionRow(
                icon = option.icon,
                title = if (isArabic) option.ar else option.en,
                subtitle = "",
                selected = active,
                onClick = { onSelect(option.key) }
            )
            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun SelectionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AyriaGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle.isBlank()) 66.dp else 78.dp)
            .clickable(onClick = onClick),
        highlighted = selected,
        shape = RoundedCornerShape(19.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) AyriaColors.teal.copy(alpha = 0.17f)
                        else Color.White.copy(alpha = 0.055f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) AyriaColors.glacier else AyriaColors.muted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = if (selected) AyriaColors.glacier else AyriaColors.pearl,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AyriaArabicFont
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = AyriaColors.muted,
                        fontSize = 10.sp,
                        fontFamily = AyriaArabicFont
                    )
                }
            }
            if (selected) {
                Text("✓", color = AyriaColors.glacier, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun InterestTile(
    option: InterestOpt,
    isArabic: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AyriaGlassPanel(
        modifier = modifier
            .height(82.dp)
            .clickable(onClick = onClick),
        highlighted = selected,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (selected) AyriaColors.glacier else AyriaColors.muted,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isArabic) option.ar else option.en,
                color = if (selected) AyriaColors.glacier else AyriaColors.pearl,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = AyriaArabicFont,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}