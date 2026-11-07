package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adaptive.UIConfig
import com.example.data.chat.ChatMessage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.GroqChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

@Composable
fun GroqChatScreen(
    viewModel: GroqChatViewModel,
    uiConfig: UIConfig,
    isArabic: Boolean
) {
    val messages  by viewModel.messages.collectAsState()
    val isTyping  by viewModel.isTyping.collectAsState()
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Animated border colors cycling cyan → amber → purple → green → cyan
    val infiniteTransition = rememberInfiniteTransition(label = "chat_border")
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "phase"
    )
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "glow"
    )

    // Interpolate color across the cycle
    val borderColor = remember(borderPhase) {
        val colors = listOf(
            Color(0xFF00E5FF), // cyan
            Color(0xFFFFAA00), // amber
            Color(0xFFBB66FF), // purple
            Color(0xFF00FF88), // green
            Color(0xFF00E5FF)  // back to cyan
        )
        val step = borderPhase * (colors.size - 1)
        val idx = step.toInt().coerceIn(0, colors.size - 2)
        val frac = step - idx
        val c1 = colors[idx]; val c2 = colors[idx + 1]
        Color(
            red   = c1.red   + (c2.red   - c1.red)   * frac,
            green = c1.green + (c2.green - c1.green) * frac,
            blue  = c1.blue  + (c2.blue  - c1.blue)  * frac
        )
    }

    LaunchedEffect(Unit) { viewModel.runBackgroundAnalysis(uiConfig) }
    LaunchedEffect(messages.size, isTyping) {
        val target = if (isTyping) messages.size else maxOf(0, messages.size - 1)
        if (messages.isNotEmpty() || isTyping) scope.launch { listState.animateScrollToItem(target) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(if (isArabic) "مسح المحادثة" else "Clear History", fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text  = { Text(if (isArabic) "سيتم حذف جميع الرسائل نهائياً. هل أنت متأكد؟" else "All messages will be permanently deleted. Proceed?", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp, lineHeight = 19.sp) },
            confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClearDialog = false }) { Text(if (isArabic) "مسح" else "Clear", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text(if (isArabic) "إلغاء" else "Cancel", color = CyberCyan, fontFamily = FontFamily.Monospace) } },
            containerColor = Color(0xFF0B1628), titleContentColor = CyberCyan, shape = RoundedCornerShape(16.dp)
        )
    }

    // Outer container with animated glowing border
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().drawBehind {
        val strokeWidth = 1.5.dp.toPx()
        val radius = 0f
        // Draw animated glow border
        drawRoundRect(color = borderColor.copy(alpha = borderGlow * 0.6f), style = Stroke(strokeWidth), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
        // Draw pulsing light spots along the border
        val perimeter = 2 * (size.width + size.height)
        val spotPos = (borderPhase * perimeter) % perimeter
        val spotX: Float; val spotY: Float
        when {
            spotPos < size.width -> { spotX = spotPos; spotY = 0f }
            spotPos < size.width + size.height -> { spotX = size.width; spotY = spotPos - size.width }
            spotPos < 2 * size.width + size.height -> { spotX = size.width - (spotPos - size.width - size.height); spotY = size.height }
            else -> { spotX = 0f; spotY = size.height - (spotPos - 2 * size.width - size.height) }
        }
        drawCircle(brush = Brush.radialGradient(listOf(borderColor.copy(alpha = 0.9f), borderColor.copy(alpha = 0.3f), Color.Transparent), center = Offset(spotX, spotY), radius = 40.dp.toPx()), radius = 40.dp.toPx(), center = Offset(spotX, spotY))
    }) {
        Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
            ChatTopBar(onClear = { showClearDialog = true }, borderColor = borderColor, glowAlpha = borderGlow)

            if (messages.isEmpty() && !isTyping) {
                ChatEmptyState(modifier = Modifier.weight(1f), isArabic = isArabic, onPrompt = { viewModel.sendMessage(it, uiConfig) })
            } else {
                LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(messages, key = { it.id }) { msg -> ChatMessageBubble(message = msg, isArabic = isArabic) }
                    if (isTyping) { item(key = "typing") { TypingIndicatorRow() } }
                }
            }

            ChatInputBar(value = inputText, onChange = { inputText = it },
                onSend = { val text = inputText.trim(); if (text.isNotBlank()) { viewModel.sendMessage(text, uiConfig); inputText = "" } },
                isEnabled = !isTyping, isArabic = isArabic, accentColor = borderColor)
        }
    }
}

@Composable
private fun ChatTopBar(onClear: () -> Unit, borderColor: Color, glowAlpha: Float) {
    Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF060D18), DeepNavy))).border(BorderStroke(0.5.dp, borderColor.copy(alpha = glowAlpha * 0.3f))).padding(horizontal = 16.dp, vertical = 14.dp)) {
        // Status dot
        Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            val pulse = rememberInfiniteTransition(label = "status")
            val dotAlpha by pulse.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "dot")
            Box(modifier = Modifier.size(7.dp).background(Color(0xFF00FF88).copy(alpha = dotAlpha), CircleShape))
            Spacer(Modifier.width(6.dp))
            Text("ONLINE", color = Color(0xFF00FF88).copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("المساعد الذكي", color = borderColor, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 2.5.sp)
            Text("LLaMA 3.3 · Groq", color = borderColor.copy(alpha = 0.45f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd).size(34.dp).background(Color.White.copy(alpha = 0.04f), CircleShape).border(1.dp, borderColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            IconButton(onClick = onClear, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.DeleteSweep, null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ChatEmptyState(modifier: Modifier = Modifier, isArabic: Boolean, onPrompt: (String) -> Unit) {
    val suggestions = if (isArabic) listOf("غيّر لون التطبيق إلى الأخضر", "اجعل الواجهة أكثر إحكاماً", "ما هي أبرز مخاطر الأمن السيبراني؟") else listOf("Change the accent color to amber", "Switch the layout to compact mode", "What are the top cybersecurity threats?")

    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "rot")

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
            // Rotating ring
            Box(modifier = Modifier.size(90.dp).drawBehind {
                val r = size.minDimension / 2f - 3.dp.toPx()
                for (i in 0..2) {
                    val angle = (rotation + i * 120f) * PI.toFloat() / 180f
                    val cx = center.x + r * cos(angle); val cy = center.y + r * sin(angle)
                    drawCircle(CyberCyan.copy(alpha = 0.6f - i * 0.15f), radius = 3.dp.toPx(), center = Offset(cx, cy))
                }
                drawCircle(CyberCyan.copy(alpha = 0.12f), radius = r, style = Stroke(1.dp.toPx()))
            })
            Box(modifier = Modifier.size(70.dp).background(CyberCyan.copy(alpha = 0.06f), CircleShape).border(1.dp, CyberCyan.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SmartToy, null, tint = CyberCyan.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(if (isArabic) "المخ الرئيسي جاهز" else "Master Brain Online", color = CyberCyan.copy(alpha = 0.9f), fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(6.dp))
        Text(if (isArabic) "اسألني عن أي شيء — أمان، ذكاء اصطناعي، إسلام، أو خصّص الواجهة" else "Ask anything — security, AI, Islam, or customize the UI.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        Spacer(Modifier.height(24.dp))
        suggestions.forEach { prompt ->
            OutlinedButton(onClick = { onPrompt(prompt) }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(20.dp)) {
                Text(prompt, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage, isArabic: Boolean) {
    when (message.role) {
        "user"       -> UserBubble(message)
        "suggestion" -> SuggestionBubble(message)
        else         -> AssistantBubble(message)
    }
}

@Composable
private fun UserBubble(message: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Box(modifier = Modifier.widthIn(max = 270.dp).background(CyberCyan.copy(alpha = 0.12f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)).border(1.dp, CyberCyan.copy(alpha = 0.28f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(message.content, color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp, lineHeight = 21.sp)
        }
        Text(formatTime(message.timestamp), color = Color.White.copy(alpha = 0.22f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 3.dp, end = 2.dp))
    }
}

@Composable
private fun AssistantBubble(message: ChatMessage) {
    val clipboard = LocalClipboardManager.current; val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(30.dp).background(CyberCyan.copy(alpha = 0.08f), CircleShape).border(1.dp, CyberCyan.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.SmartToy, null, tint = CyberCyan.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.widthIn(max = 270.dp)) {
            Box(modifier = Modifier.background(SurfaceDark, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(message.content, color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp, lineHeight = 21.sp)
            }
            Row(modifier = Modifier.padding(top = 4.dp, start = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(formatTime(message.timestamp), color = Color.White.copy(alpha = 0.22f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Icon(Icons.Rounded.ContentCopy, "Copy", tint = CyberCyan.copy(alpha = 0.38f), modifier = Modifier.size(13.dp).clickable { clipboard.setText(AnnotatedString(message.content)); Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show() })
            }
        }
    }
}

@Composable
private fun SuggestionBubble(message: ChatMessage) {
    Box(modifier = Modifier.fillMaxWidth().background(AmberGold.copy(alpha = 0.05f), RoundedCornerShape(14.dp)).border(1.dp, AmberGold.copy(alpha = 0.22f), RoundedCornerShape(14.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Lightbulb, null, tint = AmberGold, modifier = Modifier.size(15.dp).padding(top = 1.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("PROACTIVE INSIGHT", color = AmberGold, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(5.dp))
                Text(message.content, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp, lineHeight = 19.sp)
                Text(formatTime(message.timestamp), color = AmberGold.copy(alpha = 0.3f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun TypingIndicatorRow() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(26.dp).background(CyberCyan.copy(alpha = 0.08f), CircleShape).border(1.dp, CyberCyan.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.SmartToy, null, tint = CyberCyan.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(4.dp))
        repeat(3) { i ->
            val offset by transition.animateFloat(initialValue = 0f, targetValue = -5f, animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(i * 110)), label = "dot$i")
            Box(modifier = Modifier.size(7.dp).graphicsLayer { translationY = offset }.background(CyberCyan.copy(alpha = 0.65f), CircleShape))
        }
    }
}

@Composable
private fun ChatInputBar(value: String, onChange: (String) -> Unit, onSend: () -> Unit, isEnabled: Boolean, isArabic: Boolean, accentColor: Color) {
    val focusManager = LocalFocusManager.current
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF060D18), tonalElevation = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = value, onValueChange = onChange, enabled = isEnabled, modifier = Modifier.weight(1f),
                placeholder = { Text(if (isArabic) "اكتب رسالة..." else "Message Master Brain...", color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = accentColor.copy(alpha = 0.5f), unfocusedBorderColor = Color.White.copy(alpha = 0.08f), cursorColor = accentColor, focusedContainerColor = SurfaceCard, unfocusedContainerColor = SurfaceCard, disabledContainerColor = SurfaceCard, disabledTextColor = Color.White.copy(alpha = 0.4f), disabledBorderColor = Color.White.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(24.dp), maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(); focusManager.clearFocus() }),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp))
            val active = value.isNotBlank() && isEnabled
            Box(modifier = Modifier.size(46.dp).background(if (active) accentColor else Color.White.copy(alpha = 0.04f), CircleShape).border(1.dp, if (active) accentColor else Color.White.copy(alpha = 0.06f), CircleShape), contentAlignment = Alignment.Center) {
                IconButton(onClick = { onSend(); focusManager.clearFocus() }, enabled = active, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Rounded.Send, "Send", tint = if (active) Color(0xFF040810) else Color.White.copy(alpha = 0.18f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
