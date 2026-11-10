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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "تحليل الرابط بالذكاء الاصطناعي" else "AI LINK TEXT ANALYSIS",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isAr) "تقييم استدلالي للنص الظاهر فقط" else "HEURISTIC REVIEW OF THE URL STRING ONLY",
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
                    modifier = Modifier.clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Link Input Field
            OutlinedTextField(
                value = linkInput,
                onValueChange = { linkInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("PASTE RESOURCE LINK FOR NEURAL SCAN...", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp) },
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCyan.copy(alpha = 0.3f),
                    cursorColor = CyberCyan
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    if (linkInput.isNotEmpty() && !isAnalyzingLink) {
                        Text(
                            text = "[ SCAN ]",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { 
                                    viewModel.analyzeResourceLink(linkInput)
                                }
                                .padding(8.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scan Diagnostics Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                if (isAnalyzingLink) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (isAr) "يجري تحليل صياغة الرابط فقط؛ لن يتم فتحه." else "Reviewing the URL string only; the link is not being opened.",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (linkAnalysisResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isAr) "تقييم استدلالي للرابط:" else "URL-STRING HEURISTIC REVIEW:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isAr) {
                                "تنبيه: لم يتم فتح الرابط أو فحص محتوى الصفحة أو DNS أو شهادة TLS أو قواعد السمعة. هذا التقييم ليس دليلاً على أن الرابط آمن."
                            } else {
                                "Limit: this did not open the link or inspect page content, DNS, TLS certificates, or reputation feeds. This is not proof that the URL is safe."
                            },
                            color = Color(0xFFFFC66D),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = linkAnalysisResult!!,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.clearLinkAnalysis() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text("CLEAR ANALYSIS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "AWAITING NEURAL INPUT...",
                            color = Color.White.copy(alpha = 0.2f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Footer Info
            Text(
                text = "ENCRYPTION: AES-256-GCM // NEURAL_LAYER: ACTIVE",
                color = CyberCyan.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
