package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    onClose: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenCustomize: () -> Unit = {},
    onLockRequest: (String, String, () -> Unit) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isAr by viewModel.isArabic.collectAsState()
    val isStealth by viewModel.isStealthMode.collectAsState()
    val apiKey by viewModel.customApiKey.collectAsState()
    val groqApiKey by viewModel.groqApiKey.collectAsState()
    val operatorName by viewModel.operatorName.collectAsState()
    val neuralRole by viewModel.neuralRole.collectAsState()
    val uriHandler = LocalUriHandler.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    BackHandler { onClose() }

    var tempOperatorName by remember(operatorName) { mutableStateOf(operatorName) }
    var tempNeuralRole by remember(neuralRole) { mutableStateOf(neuralRole) }
    var tempApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var tempGroqApiKey by remember(groqApiKey) { mutableStateOf(groqApiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var showGroqKey by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "settings_glow")
    val borderGlow by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "glow")

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
            // Subtle top gradient
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Brush.verticalGradient(listOf(AmberZen.copy(alpha = 0.06f), Color.Transparent))))

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                // Header
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(AmberZen.copy(alpha = 0.1f), CircleShape).border(1.dp, AmberZen.copy(alpha = 0.3f), CircleShape).clickable { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onClose() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AmberZen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(if (isAr) "مركز التحكم" else "CONTROL CENTER", color = AmberZen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            Text(if (isAr) "الإعدادات والتخصيص" else "Settings & Personalization", color = AmberZen.copy(alpha = 0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 60.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // ── NEURAL PROFILE ────────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "الملف العصبي" else "NEURAL PROFILE", icon = Icons.Default.Person, color = CyberCyan, glowAlpha = borderGlow) {
                        GlowTextField(label = if (isAr) "اسم المشغل" else "OPERATOR NAME", value = tempOperatorName, onValueChange = { tempOperatorName = it; viewModel.updateOperatorName(it) }, color = CyberCyan, isAr = isAr)
                        Spacer(Modifier.height(8.dp))
                        GlowTextField(label = if (isAr) "الدور العصبي" else "NEURAL ROLE", value = tempNeuralRole, onValueChange = { tempNeuralRole = it; viewModel.updateNeuralRole(it) }, color = CyberCyan, isAr = isAr)
                        Spacer(Modifier.height(10.dp))
                        SettingsToggleRow(label = if (isAr) "اللغة العربية" else "Arabic Language", checked = isAr, onCheckedChange = { viewModel.setArabic(it) }, icon = Icons.Default.Language, color = CyberCyan)
                    }

                    // ── AI KEYS ───────────────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "مفاتيح الذكاء الاصطناعي" else "AI NEURAL KEYS", icon = Icons.Default.Key, color = AmberZen, glowAlpha = borderGlow) {
                        // Groq key
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFAA00).copy(alpha = 0.04f), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFFFAA00).copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, null, tint = AmberZen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isAr) "🔥 Groq — سريع ومجاني" else "🔥 Groq — Fast & Free", color = AmberZen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(if (isAr) "المفتاح الأساسي للدردشة والتحليل" else "Primary key for chat & analysis", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                            }
                            TextButton(onClick = { uriHandler.openUri("https://console.groq.com/keys") }) {
                                Text("احصل عليه", color = AmberZen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        PasswordField(label = if (isAr) "مفتاح Groq API" else "GROQ API KEY", value = tempGroqApiKey, onValueChange = { tempGroqApiKey = it; viewModel.updateGroqApiKey(it) }, show = showGroqKey, onToggleShow = { showGroqKey = !showGroqKey }, color = AmberZen, isAr = isAr)
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                        Spacer(Modifier.height(12.dp))
                        // Gemini key
                        Row(modifier = Modifier.fillMaxWidth().background(CyberCyan.copy(alpha = 0.04f), RoundedCornerShape(10.dp)).border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isAr) "✨ Gemini — تحليل الروابط" else "✨ Gemini — Link Analysis", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(if (isAr) "مفتاح ثانوي لتحليل الروابط والتلميحات الأمنية" else "Secondary key for link analysis & security tips", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                            }
                            TextButton(onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") }) {
                                Text(if (isAr) "احصل عليه" else "Get Key", color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        PasswordField(label = if (isAr) "مفتاح Gemini API" else "GEMINI API KEY", value = tempApiKey, onValueChange = { tempApiKey = it; viewModel.updateCustomApiKey(it) }, show = showGeminiKey, onToggleShow = { showGeminiKey = !showGeminiKey }, color = CyberCyan, isAr = isAr)
                    }

                    // ── SECURITY ──────────────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "الحماية والأمان" else "SECURITY CORE", icon = Icons.Default.Security, color = Color(0xFFFF4D6D), glowAlpha = borderGlow) {
                        SettingsToggleRow(label = if (isAr) "وضع التخفي" else "Stealth Mode", checked = isStealth, onCheckedChange = { viewModel.setStealthMode(it) }, icon = Icons.Default.VisibilityOff, color = Color(0xFFFF4D6D))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.06f))
                        // Biometric lock button
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFF4D6D).copy(alpha = 0.07f)).border(1.dp, Color(0xFFFF4D6D).copy(alpha = 0.2f), RoundedCornerShape(10.dp)).clickable { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onLockRequest(if (isAr) "قفل الجلسة" else "Lock Session", if (isAr) "تأكيد الهوية لقفل التطبيق" else "Confirm identity to lock", {}) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Fingerprint, null, tint = Color(0xFFFF4D6D), modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isAr) "قفل الجلسة" else "Lock Session", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (isAr) "بصمة الإصبع أو رمز الجهاز" else "Fingerprint or device PIN", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFFF4D6D).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }

                    // ── CUSTOMIZATION ────────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "التخصيص والمظهر" else "CUSTOMIZATION", icon = Icons.Default.Palette, color = Color(0xFFBB66FF), glowAlpha = borderGlow) {
                        // AI Customizer button
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFBB66FF).copy(alpha = 0.08f)).border(1.dp, Color(0xFFBB66FF).copy(alpha = 0.3f), RoundedCornerShape(10.dp)).clickable { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onOpenCustomize() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFBB66FF), modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isAr) "تخصيص الواجهة بالذكاء الاصطناعي" else "AI UI Customizer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (isAr) "اطلب تغييرات الألوان والخطوط والمظهر بالعربية أو الإنجليزية" else "Change colors, fonts & style with natural language", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFBB66FF).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }

                    // ── DOCUMENTATION ─────────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "التوثيق والمعلومات" else "DOCUMENTATION", icon = Icons.Default.MenuBook, color = Color(0xFF00FF88), glowAlpha = borderGlow) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF00FF88).copy(alpha = 0.05f)).border(1.dp, Color(0xFF00FF88).copy(alpha = 0.2f), RoundedCornerShape(10.dp)).clickable { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onOpenAbout() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFF00FF88), modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isAr) "دليل التشغيل الكامل" else "Full Operator Manual", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (isAr) "شرح كامل لجميع ميزات وقدرات التطبيق" else "Complete guide to all app features & capabilities", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF00FF88).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }

                    // ── DATA MANAGEMENT ───────────────────────────────────────────────
                    SettingsCard(title = if (isAr) "إدارة البيانات" else "DATA MANAGEMENT", icon = Icons.Default.Storage, color = Color(0xFFFF6B35), glowAlpha = borderGlow) {
                        Text(if (isAr) "⚠️ يحذف جميع الأفكار المحفوظة نهائياً ولا يمكن التراجع عنه" else "⚠️ Permanently deletes all saved records. Cannot be undone.", color = Color(0xFFFF6B35).copy(alpha = 0.7f), fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.Red.copy(alpha = 0.08f)).border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).clickable { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); viewModel.wipeVault() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.DeleteForever, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Text(if (isAr) "حذف جميع البيانات المحفوظة" else "DELETE ALL SAVED DATA", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Footer
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("A.SYRIA SOVEREIGN OS v5.0.0", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, textAlign = TextAlign.Center)
                        Text("BUILT BY ABOUDA.AL.SHEKH.YOSSEF", color = AmberZen.copy(alpha = 0.15f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, icon: ImageVector, color: Color, glowAlpha: Float, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = glowAlpha * 0.4f), RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.03f), RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
            Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.12f), CircleShape).border(1.dp, color.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        }
        HorizontalDivider(color = color.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 14.dp))
        content()
    }
}

@Composable
private fun GlowTextField(label: String, value: String, onValueChange: (String) -> Unit, color: Color, isAr: Boolean) {
    Column {
        Text(label, color = color.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = color.copy(alpha = 0.5f), unfocusedBorderColor = Color.White.copy(alpha = 0.1f), cursorColor = color, focusedContainerColor = color.copy(alpha = 0.04f), unfocusedContainerColor = Color.Transparent),
            shape = RoundedCornerShape(10.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, textAlign = if (isAr) TextAlign.Right else TextAlign.Left))
    }
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, show: Boolean, onToggleShow: () -> Unit, color: Color, isAr: Boolean) {
    Column {
        Text(label, color = color.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = onToggleShow) { Icon(if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) } },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = color.copy(alpha = 0.5f), unfocusedBorderColor = Color.White.copy(alpha = 0.1f), cursorColor = color, focusedContainerColor = color.copy(alpha = 0.04f), unfocusedContainerColor = Color.Transparent),
            shape = RoundedCornerShape(10.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp))
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Text(label, color = Color.White, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = color, checkedTrackColor = color.copy(alpha = 0.3f), uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.Gray.copy(alpha = 0.15f)))
    }
}

// Keep these for backward compatibility (used elsewhere)
@Composable
fun SettingsSectionHeader(title: String) {
    Column {
        Text(title, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTextField(label: String, value: String, onValueChange: (String) -> Unit, isAr: Boolean, isPassword: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp), fontFamily = FontFamily.Monospace)
        TextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White.copy(alpha = 0.05f), unfocusedContainerColor = Color.White.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = CyberCyan),
            singleLine = true)
    }
}

@Composable
fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector, tint: Color = CyberCyan) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onCheckedChange(!checked) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = tint, checkedTrackColor = tint.copy(alpha = 0.3f), uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.Gray.copy(alpha = 0.1f)))
    }
}
