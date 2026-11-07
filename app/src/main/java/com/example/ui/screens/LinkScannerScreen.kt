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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkScannerScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    var linkInput by remember { mutableStateOf("") }
    val isAnalyzingLink by viewModel.isAnalyzingLink.collectAsState()
    val linkAnalysisResult by viewModel.linkAnalysisResult.collectAsState()
    val isAr by viewModel.isArabic.collectAsState(initial = false)

    val scanLogs = remember { mutableStateListOf<String>() }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isAnalyzingLink) {
        if (isAnalyzingLink) {
            scanLogs.clear()
            progress.snapTo(0f)
            val logSequence = listOf(
                "CONNECTING TO NEURAL_CORE...",
                "INTERCEPTING DATA PACKETS...",
                "CIPHER: 0x${(1000..9999).random().toString(16).uppercase()}",
                "ANALYZING SECURITY HEADERS...",
                "GEMINI_UPLINK ESTABLISHED",
                "DECODING PAYLOAD...",
                "RECOGNIZING PATTERNS...",
                "SCAN_COMPLETE // GENERATING REPORT"
            )
            
            // Progress animation synchronized with logs
            for (log in logSequence) {
                scanLogs.add("> $log")
                progress.animateTo(
                    targetValue = scanLogs.size.toFloat() / logSequence.size,
                    animationSpec = tween(400)
                )
                delay(300)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
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
                        text = "INTERNAL SCAN",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "NEURAL LINK DECODER v4.2",
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Hex Progress Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SCANNING [",
                                color = CyberCyan.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            LinearProgressIndicator(
                                progress = progress.value,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .padding(horizontal = 4.dp),
                                color = CyberCyan,
                                trackColor = CyberCyan.copy(alpha = 0.1f)
                            )
                            Text(
                                text = "] ${(progress.value * 100).toInt()}%",
                                color = CyberCyan.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(scanLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("COMPLETE")) AmberZen else Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else if (linkAnalysisResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isAr) "تقرير التشفير السيادي:" else "SOVEREIGN DECODING REPORT:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
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
