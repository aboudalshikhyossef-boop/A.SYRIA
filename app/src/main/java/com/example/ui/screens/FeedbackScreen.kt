package com.example.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.animation.core.*
  import androidx.compose.foundation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.AutoFixHigh
  import androidx.compose.material.icons.filled.Check
  import androidx.compose.material.icons.filled.Close
  import androidx.compose.material.icons.filled.Timer
  import androidx.compose.material.icons.filled.Warning
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.platform.LocalContext
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.example.adaptive.FeedbackResult
  import com.example.adaptive.RateLimiter
  import com.example.adaptive.UIChange
  import com.example.ui.theme.CyberCyan
  import com.example.ui.viewmodel.DashboardViewModel
  import kotlinx.coroutines.delay
  import kotlinx.coroutines.launch

  private val CyberAmber = Color(0xFFFFAA00)
  private val CyberGreen = Color(0xFF00FF88)

  @Composable
  fun FeedbackScreen(
      viewModel: DashboardViewModel,
      isArabic: Boolean,
      onBack: () -> Unit
  ) {
      val context = LocalContext.current
      val scope   = rememberCoroutineScope()

      // ── State ────────────────────────────────────────────────────────────────
      var feedbackText by remember { mutableStateOf("") }
      var isLoading    by remember { mutableStateOf(false) }
      var result       by remember { mutableStateOf<FeedbackResult?>(null) }
      var selectedKeys by remember { mutableStateOf(setOf<String>()) }

      // Rate-limit display (refreshes every second while blocked)
      var remaining    by remember { mutableIntStateOf(RateLimiter.getRemainingCount(context)) }
      var resetSecs    by remember { mutableLongStateOf(RateLimiter.getResetSeconds(context)) }

      LaunchedEffect(Unit) {
          while (true) {
              remaining = RateLimiter.getRemainingCount(context)
              resetSecs = RateLimiter.getResetSeconds(context)
              delay(1000)
          }
      }

      // ── Helpers ──────────────────────────────────────────────────────────────
      fun formatCountdown(secs: Long): String {
          val h = secs / 3600; val m = (secs % 3600) / 60; val s = secs % 60
          return if (h > 0) "${h}h ${m}m ${s}s" else "${m}m ${s}s"
      }

      // ── Layout ───────────────────────────────────────────────────────────────
      Column(
          modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF040D14))
              .verticalScroll(rememberScrollState())
              .padding(20.dp)
      ) {

          // ── Header ───────────────────────────────────────────────────────────
          Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = if (isArabic) "🡠 عودة" else "🡠 BACK",
                  color = CyberAmber, fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                  modifier = Modifier.clickable(onClick = onBack).padding(vertical = 4.dp)
              )
          }

          Spacer(Modifier.height(20.dp))

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
              Text(
                  text = if (isArabic) "تخصيص الواجهة بالذكاء الاصطناعي" else "AI-POWERED UI CUSTOMIZER",
                  color = CyberCyan, fontSize = 13.sp,
                  fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
              )
          }
          Text(
              text = if (isArabic)
                  "اكتب ما تريد تغييره بكلماتك — الذكاء الاصطناعي سيقترح التعديلات وأنت تؤكدها."
              else
                  "Describe what you want changed in plain words. AI suggests edits — you confirm before anything applies.",
              color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp,
              lineHeight = 16.sp, fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(top = 6.dp)
          )

          Spacer(Modifier.height(20.dp))

          // ── Rate-limit badge ─────────────────────────────────────────────────
          val isBlocked = remaining == 0 && resetSecs > 0
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, (if (isBlocked) Color.Red else CyberGreen).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                  .background((if (isBlocked) Color.Red else CyberGreen).copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                  .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      if (isBlocked) Icons.Default.Timer else Icons.Default.Check,
                      contentDescription = null,
                      tint = if (isBlocked) Color.Red else CyberGreen,
                      modifier = Modifier.size(14.dp)
                  )
                  Text(
                      text = if (isBlocked)
                          (if (isArabic) "الحد الأقصى مكتمل — إعادة تعيين خلال" else "Limit reached — resets in")
                      else
                          (if (isArabic) "الطلبات المتبقية" else "Requests remaining"),
                      color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace
                  )
              }
              Text(
                  text = if (isBlocked) formatCountdown(resetSecs) else "${remaining} / 3",
                  color = if (isBlocked) Color.Red else CyberGreen,
                  fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
              )
          }

          Spacer(Modifier.height(16.dp))

          // ── Feedback text field ───────────────────────────────────────────────
          OutlinedTextField(
              value = feedbackText,
              onValueChange = { if (it.length <= 300) feedbackText = it },
              modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
              placeholder = {
                  Text(
                      text = if (isArabic)
                          "مثال: أريد خطاً أكبر، ولوناً أصفر، وأخفِ تبويب الأكاديمية..."
                      else
                          "Example: I want larger text, amber color, hide the academy tab...",
                      color = Color.White.copy(alpha = 0.25f),
                      fontSize = 11.sp, fontFamily = FontFamily.Monospace
                  )
              },
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyberCyan,
                  unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                  cursorColor = CyberCyan,
                  focusedContainerColor = Color.White.copy(alpha = 0.02f),
                  unfocusedContainerColor = Color.Transparent
              ),
              shape = RoundedCornerShape(10.dp),
              maxLines = 6
          )

          Text(
              text = "${feedbackText.length}/300",
              color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp,
              fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
          )

          Spacer(Modifier.height(14.dp))

          // ── Submit button ─────────────────────────────────────────────────────
          Button(
              onClick = {
                  if (feedbackText.isBlank() || isBlocked) return@Button
                  isLoading = true
                  result = null
                  selectedKeys = emptySet()
                  scope.launch {
                      result = viewModel.submitFeedback(feedbackText)
                      isLoading = false
                  }
              },
              enabled = feedbackText.isNotBlank() && !isBlocked && !isLoading,
              colors = ButtonDefaults.buttonColors(
                  containerColor = CyberCyan.copy(alpha = 0.12f),
                  disabledContainerColor = Color.White.copy(alpha = 0.03f)
              ),
              border = BorderStroke(1.dp, if (!isBlocked && feedbackText.isNotBlank()) CyberCyan else Color.White.copy(alpha = 0.1f)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp)
          ) {
              if (isLoading) {
                  CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                  Spacer(Modifier.width(10.dp))
              }
              Text(
                  text = if (isLoading) (if (isArabic) "جاري التحليل..." else "ANALYZING...")
                         else (if (isArabic) "⚡ تحليل الطلب بالذكاء الاصطناعي" else "⚡ ANALYZE WITH AI"),
                  color = if (!isBlocked && feedbackText.isNotBlank()) CyberCyan else Color.White.copy(alpha = 0.3f),
                  fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
              )
          }

          Spacer(Modifier.height(20.dp))

          // ── Result area ───────────────────────────────────────────────────────
          AnimatedVisibility(visible = result != null, enter = fadeIn() + expandVertically()) {
              when (val r = result) {
                  is FeedbackResult.Success -> SuccessPanel(
                      changes       = r.changes,
                      selectedKeys  = selectedKeys,
                      isArabic      = isArabic,
                      onToggle      = { key -> selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key },
                      onApply       = {
                          val toApply = r.changes.filter { it.key in selectedKeys }
                          if (toApply.isNotEmpty()) {
                              viewModel.applyUIChanges(toApply)
                              result = null; feedbackText = ""
                          }
                      }
                  )
                  is FeedbackResult.RateLimited -> MessageCard(
                      icon    = Icons.Default.Timer,
                      color   = Color.Red,
                      message = if (isArabic)
                          "الحد الأقصى للطلبات مكتمل. يُعاد تعيينه خلال ${formatCountdown(r.resetSeconds)}"
                      else
                          "Request limit reached. Resets in ${formatCountdown(r.resetSeconds)}"
                  )
                  is FeedbackResult.Empty -> MessageCard(
                      icon    = Icons.Default.Warning,
                      color   = CyberAmber,
                      message = r.message
                  )
                  is FeedbackResult.Error -> MessageCard(
                      icon    = Icons.Default.Close,
                      color   = Color.Red,
                      message = r.message
                  )
                  null -> {}
              }
          }

          Spacer(Modifier.height(32.dp))

          // ── Reset defaults ────────────────────────────────────────────────────
          TextButton(
              onClick = { viewModel.resetUIToDefaults() },
              modifier = Modifier.align(Alignment.CenterHorizontally)
          ) {
              Text(
                  text = if (isArabic) "↺ إعادة الواجهة للافتراضي" else "↺ Reset UI to defaults",
                  color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace
              )
          }
      }
  }

  // ── Sub-composables ────────────────────────────────────────────────────────────

  @Composable
  private fun SuccessPanel(
      changes: List<UIChange>,
      selectedKeys: Set<String>,
      isArabic: Boolean,
      onToggle: (String) -> Unit,
      onApply: () -> Unit
  ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
              text = if (isArabic) "✦ التغييرات المقترحة — اختر ما تريد تطبيقه:" else "✦ PROPOSED CHANGES — Select to apply:",
              color = CyberCyan, fontSize = 11.sp,
              fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
          )

          changes.forEach { change ->
              val isSelected = change.key in selectedKeys
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .border(
                          1.dp,
                          if (isSelected) CyberGreen else Color.White.copy(alpha = 0.12f),
                          RoundedCornerShape(10.dp)
                      )
                      .background(
                          if (isSelected) CyberGreen.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.02f),
                          RoundedCornerShape(10.dp)
                      )
                      .clickable { onToggle(change.key) }
                      .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                  Checkbox(
                      checked = isSelected,
                      onCheckedChange = { onToggle(change.key) },
                      colors = CheckboxDefaults.colors(
                          checkedColor = CyberGreen,
                          uncheckedColor = Color.White.copy(alpha = 0.3f),
                          checkmarkColor = Color(0xFF040D14)
                      )
                  )
                  Column {
                      Text(change.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                      if (change.preview.isNotBlank()) {
                          Text(change.preview, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp, lineHeight = 14.sp, fontFamily = FontFamily.Monospace)
                      }
                  }
              }
          }

          Spacer(Modifier.height(6.dp))

          Button(
              onClick = onApply,
              enabled = selectedKeys.isNotEmpty(),
              colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.12f)),
              border = BorderStroke(1.dp, if (selectedKeys.isNotEmpty()) CyberGreen else Color.White.copy(alpha = 0.1f)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp)
          ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = if (selectedKeys.isNotEmpty()) CyberGreen else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                  text = if (isArabic) "✅ تطبيق التغييرات المختارة" else "✅ APPLY SELECTED CHANGES",
                  color = if (selectedKeys.isNotEmpty()) CyberGreen else Color.White.copy(alpha = 0.3f),
                  fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
              )
          }
      }
  }

  @Composable
  private fun MessageCard(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, message: String) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .background(color.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
              .padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.Top
      ) {
          Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
          Text(message, color = color.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = FontFamily.Monospace)
      }
  }
  