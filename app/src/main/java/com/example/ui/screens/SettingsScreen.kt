package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
    onOpenAppLock: () -> Unit = {}
) {
    val isAr by viewModel.isArabic.collectAsState()
    val appPinEnabled by viewModel.isAppPinEnabled.collectAsState()
    val apiKey by viewModel.customApiKey.collectAsState()
    var tempApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    val transition = rememberInfiniteTransition(label = "settings_frost")
    val edgeGlow by transition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "settings_edge_glow"
    )

    BackHandler { onClose() }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (isAr) "الإعدادات" else "SETTINGS",
                            color = Color(0xFFD8F1FA),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            if (isAr) "إعدادان أساسيان فقط" else "ONLY THE ESSENTIAL CONTROLS",
                            color = Color(0xFFA7C5D4).copy(alpha = 0.58f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isAr) "إغلاق الإعدادات" else "Close settings",
                            tint = Color(0xFFC5E6F0)
                        )
                    }
                }

                SettingsCard(
                    title = if (isAr) "مفتاح Gemini API" else "GEMINI API KEY",
                    icon = Icons.Default.Key,
                    color = Color(0xFF9BDCF0),
                    glowAlpha = edgeGlow
                ) {
                    Text(
                        if (isAr) {
                            "أدخل مفتاح Gemini لاستخدام المساعد الذكي. يبقى المفتاح ضمن إعدادات التطبيق على هذا الجهاز."
                        } else {
                            "Add your Gemini API key to enable the AI assistant. The key is stored in this app's local preferences."
                        },
                        color = Color(0xFFC4DAE4).copy(alpha = 0.68f),
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    PasswordField(
                        label = if (isAr) "مفتاح API" else "API KEY",
                        value = tempApiKey,
                        onValueChange = {
                            tempApiKey = it
                            viewModel.updateCustomApiKey(it)
                        },
                        show = showGeminiKey,
                        onToggleShow = { showGeminiKey = !showGeminiKey },
                        color = Color(0xFF9BDCF0),
                        isAr = isAr
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(7.dp)
                                .background(
                                    if (apiKey.isNotBlank()) Color(0xFF9FE0D0) else Color(0xFF8EA9B9),
                                    CircleShape
                                )
                        )
                        Text(
                            if (apiKey.isNotBlank()) {
                                if (isAr) "تم إدخال مفتاح API" else "API KEY CONFIGURED"
                            } else {
                                if (isAr) "لم يتم إدخال المفتاح بعد" else "API KEY NOT SET"
                            },
                            color = Color(0xFFAFC9D5).copy(alpha = 0.66f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                SettingsCard(
                    title = if (isAr) "قفل التطبيق" else "APP LOCK",
                    icon = Icons.Default.Lock,
                    color = Color(0xFFB6DCEB),
                    glowAlpha = edgeGlow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0x2B8AC6DB), Color(0x151A455E), Color(0x14FFFFFF))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .border(0.8.dp, Color(0x557EBAD0), RoundedCornerShape(20.dp))
                            .clickable { onOpenAppLock() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0x2B9BD7EB), CircleShape)
                                .border(0.8.dp, Color(0x668AC9DE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFFD7F0F8), modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (isAr) "إدارة رمز قفل التطبيق" else "Manage app PIN",
                                color = Color(0xFFE2F4FA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (appPinEnabled) {
                                    if (isAr) "القفل مفعّل · اضغط لإدارة الرمز" else "LOCK ENABLED · TAP TO MANAGE"
                                } else {
                                    if (isAr) "القفل غير مفعّل · اضغط لإعداد رمز" else "LOCK DISABLED · TAP TO SET UP"
                                },
                                color = Color(0xFFA9C8D6).copy(alpha = 0.68f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFFB5DBE9).copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        if (isAr) {
                            "قفل محلي مستقل عن قفل شاشة الهاتف. يمكنك تفعيله أو تغيير رمزه من هنا."
                        } else {
                            "A local app-only lock, separate from your phone's screen lock. Enable it or change the PIN here."
                        },
                        color = Color(0xFFABC4D0).copy(alpha = 0.55f),
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(Modifier.weight(1f))
                Text(
                    "A.SYRIA  ·  QUIET CONTROL",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    color = Color(0xFFA5C3D0).copy(alpha = 0.30f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.4.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, icon: ImageVector, color: Color, glowAlpha: Float, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = glowAlpha * 0.4f), RoundedCornerShape(24.dp)).background(color.copy(alpha = 0.03f), RoundedCornerShape(24.dp)).padding(16.dp)) {
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
