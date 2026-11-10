package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkScannerScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    var linkInput by remember { mutableStateOf("") }
    val isAnalyzingLink by viewModel.isAnalyzingLink.collectAsState()
    val linkAnalysisResult by viewModel.linkAnalysisResult.collectAsState()
    val isAr by viewModel.isArabic.collectAsState(initial = false)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides
            if (isAr) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(8.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE91B3B50), Color(0xF10C1F32), Color(0xF2071524))
                    ),
                    RoundedCornerShape(30.dp)
                )
                .border(
                    0.9.dp,
                    Brush.linearGradient(listOf(Color(0x779BD8E8), Color(0x264B91AD), Color(0x22FFFFFF))),
                    RoundedCornerShape(30.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (isAr) "مصادر وفحص الروابط" else "SOURCE INSPECTOR",
                            color = Color(0xFFDDF3FA),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            if (isAr) "مراجعة نص الرابط محلياً" else "LOCAL URL-STRING REVIEW",
                            color = Color(0xFFA6C5D3).copy(alpha = 0.62f),
                            fontSize = 8.sp,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onBack()
                        }
                    ) {
                        Text(if (isAr) "إغلاق" else "CLOSE", color = Color(0xFFC8E9F2), fontSize = 10.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(22.dp))
                        .border(0.8.dp, Color(0x448CC7DB), RoundedCornerShape(22.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        if (isAr) "أدخل رابطاً لمراجعة مؤشراته الظاهرة" else "PASTE A URL TO REVIEW ITS VISIBLE SIGNALS",
                        color = Color(0xFFD0EAF3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (isAr) FontFamily.Default else FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = linkInput,
                        onValueChange = { linkInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                if (isAr) "https://example.com" else "https://example.com",
                                color = Color(0xFFA5C0CE).copy(alpha = 0.48f),
                                fontSize = 12.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFFE8F5F9),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9CD8E9),
                            unfocusedBorderColor = Color(0x557BAFC3),
                            cursorColor = Color(0xFFBCEAF5),
                            focusedContainerColor = Color(0x66102A3E),
                            unfocusedContainerColor = Color(0x66102A3E)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.analyzeResourceLink(linkInput)
                        },
                        enabled = linkInput.isNotBlank() && !isAnalyzingLink,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9CCFDF),
                            contentColor = Color(0xFF092033),
                            disabledContainerColor = Color(0x337AAFC2),
                            disabledContentColor = Color(0xFF9FBCC8).copy(alpha = 0.55f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isAnalyzingLink) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF0B263A),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(9.dp))
                        }
                        Text(
                            if (isAnalyzingLink) {
                                if (isAr) "جارٍ التحليل..." else "REVIEWING..."
                            } else {
                                if (isAr) "تحليل الرابط" else "INSPECT URL"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.linearGradient(listOf(Color(0x661B3A50), Color(0x55102032))),
                            RoundedCornerShape(24.dp)
                        )
                        .border(0.8.dp, Color(0x337FB6CC), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        if (isAr) "نتيجة المراجعة" else "REVIEW DETAILS",
                        color = Color(0xFFCDEAF3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    when {
                        isAnalyzingLink -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color(0xFFB6E2EF), strokeWidth = 2.dp)
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    if (isAr) "تتم مراجعة بنية الرابط فقط" else "Reviewing the URL string only",
                                    color = Color(0xFFB8D0DC),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        linkAnalysisResult != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    if (isAr) {
                                        "تنبيه: لم يتم فتح الرابط أو فحص محتوى الصفحة أو DNS أو شهادة TLS أو قواعد السمعة. النتيجة ليست دليلاً على أن الرابط آمن."
                                    } else {
                                        "Limit: the link was not opened and page content, DNS, TLS certificates or reputation feeds were not checked. This result is not proof that the URL is safe."
                                    },
                                    color = Color(0xFFE5D7B5),
                                    fontSize = 10.sp,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    linkAnalysisResult.orEmpty(),
                                    color = Color(0xFFE3F1F6),
                                    fontSize = 12.sp,
                                    lineHeight = 19.sp
                                )
                                TextButton(onClick = { viewModel.clearLinkAnalysis() }) {
                                    Text(if (isAr) "مسح النتيجة" else "CLEAR REVIEW", color = Color(0xFFB6E2EF))
                                }
                            }
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("◇", color = Color(0xFFB6DCE8).copy(alpha = 0.45f), fontSize = 34.sp)
                                Spacer(Modifier.height(9.dp))
                                Text(
                                    if (isAr) "بانتظار الرابط" else "WAITING FOR A URL",
                                    color = Color(0xFFB5D0DC).copy(alpha = 0.72f),
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    if (isAr) "ستظهر هنا مؤشرات المراجعة بعد بدء التحليل." else "Review signals will appear here after inspection.",
                                    color = Color(0xFFA8C3D0).copy(alpha = 0.48f),
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Text(
                    if (isAr) "المراجعة نصية استدلالية فقط · لا تفتح الرابط" else "STRING HEURISTICS ONLY · THE LINK IS NOT OPENED",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF9DB9C7).copy(alpha = 0.42f),
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
