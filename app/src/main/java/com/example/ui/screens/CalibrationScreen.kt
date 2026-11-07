package com.example.ui.screens

  import androidx.compose.animation.AnimatedContent
  import androidx.compose.animation.fadeIn
  import androidx.compose.animation.fadeOut
  import androidx.compose.animation.togetherWith
  import androidx.compose.animation.core.tween
  import androidx.compose.foundation.background
  import androidx.compose.foundation.border
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.rememberScrollState
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.foundation.verticalScroll
  import androidx.compose.material3.LinearProgressIndicator
  import androidx.compose.material3.Text
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.example.ui.theme.CyberCyan
  import com.example.ui.theme.VoidBlack
  import com.example.ui.theme.cascadiaCode
  import com.example.ui.viewmodel.DashboardViewModel

  private data class InterestOpt(val key: String, val ar: String, val en: String, val icon: String)
  private data class GoalOpt(val key: String, val ar: String, val en: String, val icon: String)

  @Composable
  fun CalibrationScreen(
      viewModel: DashboardViewModel,
      onCalibrationComplete: () -> Unit
  ) {
      val isAr by viewModel.isArabic.collectAsState()
      var step by remember { mutableIntStateOf(0) }
      val totalSteps = 3

      val levels = listOf(
          Triple("Beginner", "\u0645\u0628\u062a\u062f\u0626", "\u26a1"),
          Triple("Intermediate", "\u0645\u062a\u0648\u0633\u0637", "\uD83D\uDD25"),
          Triple("Advanced", "\u0645\u062a\u0642\u062f\u0645", "\uD83D\uDC8E")
      )

      val interestOpts = listOf(
          InterestOpt("Account Security", "\u0623\u0645\u0646 \u0627\u0644\u062d\u0633\u0627\u0628\u0627\u062a", "Account Security", "\uD83D\uDD10"),
          InterestOpt("Network Security", "\u0623\u0645\u0646 \u0627\u0644\u0634\u0628\u0643\u0627\u062a", "Network Security", "\uD83C\uDF10"),
          InterestOpt("Phishing & Malware", "\u0627\u0644\u062a\u0635\u064a\u062f \u0648\u0627\u0644\u0628\u0631\u0645\u062c\u064a\u0627\u062a \u0627\u0644\u062e\u0628\u064a\u062b\u0629", "Phishing & Malware", "\uD83C\uDFA3"),
          InterestOpt("Ethical Hacking", "\u0627\u0644\u0627\u062e\u062a\u0631\u0627\u0642 \u0627\u0644\u0623\u062e\u0644\u0627\u0642\u064a", "Ethical Hacking", "\uD83E\uDDE0"),
          InterestOpt("Digital Privacy", "\u0627\u0644\u062e\u0635\u0648\u0635\u064a\u0629 \u0627\u0644\u0631\u0642\u0645\u064a\u0629", "Digital Privacy", "\uD83D\uDEE1\uFE0F"),
          InterestOpt("Mobile Security", "\u0623\u0645\u0646 \u0627\u0644\u0647\u0648\u0627\u062a\u0641", "Mobile Security", "\uD83D\uDCF1"),
          InterestOpt("Social Engineering", "\u0627\u0644\u0647\u0646\u062f\u0633\u0629 \u0627\u0644\u0627\u062c\u062a\u0645\u0627\u0639\u064a\u0629", "Social Engineering", "\uD83C\uDFAD"),
          InterestOpt("Cryptography", "\u0627\u0644\u062a\u0634\u0641\u064a\u0631", "Cryptography", "\uD83D\uDD12"),
          InterestOpt("Threat Intelligence", "\u0627\u0633\u062a\u062e\u0628\u0627\u0631\u0627\u062a \u0627\u0644\u062a\u0647\u062f\u064a\u062f\u0627\u062a", "Threat Intelligence", "\uD83D\uDD75\uFE0F"),
          InterestOpt("Incident Response", "\u0627\u0644\u0627\u0633\u062a\u062c\u0627\u0628\u0629 \u0644\u0644\u062d\u0648\u0627\u062f\u062b", "Incident Response", "\uD83D\uDEA8")
      )

      val goalOpts = listOf(
          GoalOpt("self_protect", "\u062d\u0645\u0627\u064a\u0629 \u0646\u0641\u0633\u064a \u0648\u0639\u0627\u0626\u0644\u062a\u064a", "Protect myself & family", "\uD83C\uDFE0"),
          GoalOpt("career", "\u0627\u0644\u062f\u062e\u0648\u0644 \u0625\u0644\u0649 \u0645\u062c\u0627\u0644 \u0627\u0644\u0623\u0645\u0646 \u0627\u0644\u0633\u064a\u0628\u0631\u0627\u0646\u064a", "Enter cybersecurity field", "\uD83D\uDCBC"),
          GoalOpt("certification", "\u0627\u062c\u062a\u064a\u0627\u0632 \u0634\u0647\u0627\u062f\u0629 \u0627\u062d\u062a\u0631\u0627\u0641\u064a\u0629", "Pass a certification", "\uD83D\uDCDC"),
          GoalOpt("threat_hunting", "\u0635\u064a\u062f \u0627\u0644\u062a\u0647\u062f\u064a\u062f\u0627\u062a \u0627\u062d\u062a\u0631\u0627\u0641\u064a\u0627\u064b", "Professional threat hunting", "\uD83C\uDFAF"),
          GoalOpt("general", "\u062a\u062d\u0633\u064a\u0646 \u0645\u0639\u0631\u0641\u062a\u064a \u0627\u0644\u0639\u0627\u0645\u0629", "Improve general knowledge", "\uD83D\uDCDA")
      )

      var selectedLevelKey by remember { mutableStateOf("Beginner") }
      val selectedInterests = remember { mutableStateListOf<String>() }
      var selectedGoal by remember { mutableStateOf("") }
      val scrollState = rememberScrollState()

      Box(
          modifier = Modifier
              .fillMaxSize()
              .background(VoidBlack)
              .statusBarsPadding()
      ) {
          Column(
              modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(scrollState)
                  .padding(horizontal = 24.dp)
                  .padding(top = 48.dp, bottom = 40.dp),
              horizontalAlignment = Alignment.CenterHorizontally
          ) {
              // Header
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
              ) {
                  Text(
                      text = if (isAr) "\u0645\u0639\u0627\u064a\u0631\u0629 \u0627\u0644\u0646\u0638\u0627\u0645" else "SYSTEM CALIBRATION",
                      color = CyberCyan,
                      fontSize = 14.sp,
                      fontFamily = cascadiaCode,
                      fontWeight = FontWeight.Bold
                  )
                  Text(
                      text = "${step + 1} / $totalSteps",
                      color = Color.White.copy(alpha = 0.5f),
                      fontSize = 13.sp,
                      fontFamily = cascadiaCode
                  )
              }
              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                  progress = { (step + 1f) / totalSteps },
                  modifier = Modifier.fillMaxWidth().height(2.dp),
                  color = CyberCyan,
                  trackColor = Color.White.copy(alpha = 0.1f)
              )
              Spacer(modifier = Modifier.height(40.dp))

              AnimatedContent(
                  targetState = step,
                  transitionSpec = {
                      fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                  },
                  label = "calStep"
              ) { s ->
                  when (s) {
                      0 -> CalStepLevel(isAr, levels, selectedLevelKey) { selectedLevelKey = it }
                      1 -> CalStepInterests(isAr, interestOpts, selectedInterests)
                      2 -> CalStepGoal(isAr, goalOpts, selectedGoal) { selectedGoal = it }
                  }
              }

              Spacer(modifier = Modifier.height(48.dp))

              val canProceed = when (step) {
                  0 -> true
                  1 -> selectedInterests.isNotEmpty()
                  2 -> selectedGoal.isNotEmpty()
                  else -> false
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                  if (step > 0) {
                      Box(
                          modifier = Modifier
                              .weight(1f)
                              .clip(RoundedCornerShape(10.dp))
                              .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                              .clickable { step-- }
                              .padding(vertical = 16.dp),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = if (isAr) "\u2190 \u0631\u062c\u0648\u0639" else "\u2190 Back",
                              color = CyberCyan, fontFamily = cascadiaCode, fontWeight = FontWeight.SemiBold
                          )
                      }
                  }
                  Box(
                      modifier = Modifier
                          .weight(if (step > 0) 1.5f else 1f)
                          .clip(RoundedCornerShape(10.dp))
                          .background(if (canProceed) CyberCyan else CyberCyan.copy(alpha = 0.3f))
                          .clickable(enabled = canProceed) {
                              if (step == totalSteps - 1) {
                                  viewModel.saveCalibrationData(selectedLevelKey, selectedInterests.toSet(), selectedGoal)
                                  onCalibrationComplete()
                              } else { step++ }
                          }
                          .padding(vertical = 16.dp),
                      contentAlignment = Alignment.Center
                  ) {
                      Text(
                          text = if (step == totalSteps - 1) {
                              if (isAr) "\u0627\u0628\u062f\u0623 \u0627\u0644\u062a\u062c\u0631\u0628\u0629 \u2192" else "START \u2192"
                          } else {
                              if (isAr) "\u0627\u0644\u062a\u0627\u0644\u064a \u2192" else "Next \u2192"
                          },
                          color = if (canProceed) Color.Black else Color.Black.copy(alpha = 0.4f),
                          fontFamily = cascadiaCode,
                          fontWeight = FontWeight.Bold,
                          fontSize = 16.sp
                      )
                  }
              }
          }
      }
  }

  @Composable
  private fun CalStepLevel(isAr: Boolean, levels: List<Triple<String,String,String>>, selectedKey: String, onSelect: (String) -> Unit) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("\u2699\uFE0F", fontSize = 52.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = if (isAr) "\u0645\u0627 \u0647\u0648 \u0645\u0633\u062a\u0648\u0627\u0643\u061f" else "What is your level?",
              color = CyberCyan, fontSize = 26.sp, fontFamily = cascadiaCode, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
          )
          Text(
              text = if (isAr) "\u0633\u064a\u062a\u0643\u064a\u0641 \u0627\u0644\u0646\u0638\u0627\u0645 \u0645\u0639 \u0645\u0633\u062a\u0648\u0627\u0643 \u062a\u0644\u0642\u0627\u0626\u064a\u0627\u064b" else "The AI adapts scenarios to your skill level",
              color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, fontFamily = cascadiaCode, textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
          )
          levels.forEach { (key, arLabel, icon) ->
              val isSelected = selectedKey == key
              val label = if (isAr) arLabel else key
              val desc = when (key) {
                  "Beginner" -> if (isAr) "\u062c\u062f\u064a\u062f \u0641\u064a \u0639\u0627\u0644\u0645 \u0627\u0644\u0623\u0645\u0646 \u0627\u0644\u0633\u064a\u0628\u0631\u0627\u0646\u064a" else "New to cybersecurity"
                  "Intermediate" -> if (isAr) "\u0644\u062f\u064a\u0643 \u0645\u0639\u0631\u0641\u0629 \u0628\u0628\u0639\u0636 \u0627\u0644\u0645\u0641\u0627\u0647\u064a\u0645 \u0627\u0644\u0623\u0633\u0627\u0633\u064a\u0629" else "Familiar with basic concepts"
                  "Advanced" -> if (isAr) "\u0645\u062d\u062a\u0631\u0641 \u0623\u0648 \u0628\u0627\u062d\u062b \u0623\u0645\u0646\u064a" else "Security professional or researcher"
                  else -> ""
              }
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSelected) CyberCyan.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                      .border(if (isSelected) 2.dp else 1.dp, if (isSelected) CyberCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                      .clickable { onSelect(key) }
                      .padding(16.dp)
              ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(icon, fontSize = 30.sp)
                      Spacer(modifier = Modifier.width(16.dp))
                      Column {
                          Text(label, color = if (isSelected) CyberCyan else Color.White, fontSize = 18.sp, fontFamily = cascadiaCode, fontWeight = FontWeight.Bold)
                          Text(desc, color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp, fontFamily = cascadiaCode)
                      }
                      Spacer(modifier = Modifier.weight(1f))
                      if (isSelected) Text("\u2713", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                  }
              }
          }
      }
  }

  @Composable
  private fun CalStepInterests(isAr: Boolean, opts: List<InterestOpt>, selected: MutableList<String>) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("\uD83C\uDFAF", fontSize = 52.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = if (isAr) "\u0645\u0627 \u0647\u064a \u0627\u0647\u062a\u0645\u0627\u0645\u0627\u062a\u0643\u061f" else "What are your interests?",
              color = CyberCyan, fontSize = 26.sp, fontFamily = cascadiaCode, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
          )
          Text(
              text = if (isAr) "\u0627\u062e\u062a\u0631 \u0645\u0627 \u064a\u0635\u0644 \u0625\u0644\u0649 3 \u0645\u062c\u0627\u0644\u0627\u062a (\u062a\u0645 \u0627\u062e\u062a\u064a\u0627\u0631: ${selected.size}/3)" else "Choose up to 3 areas (${selected.size}/3 selected)",
              color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, fontFamily = cascadiaCode, textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
          )
          opts.chunked(2).forEach { row ->
              Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  row.forEach { opt ->
                      val isSel = selected.contains(opt.key)
                      Box(
                          modifier = Modifier
                              .weight(1f)
                              .clip(RoundedCornerShape(10.dp))
                              .background(if (isSel) CyberCyan.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                              .border(if (isSel) 2.dp else 1.dp, if (isSel) CyberCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                              .clickable {
                                  if (isSel) selected.remove(opt.key)
                                  else if (selected.size < 3) selected.add(opt.key)
                              }
                              .padding(12.dp),
                          contentAlignment = Alignment.Center
                      ) {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Text(opt.icon, fontSize = 24.sp)
                              Spacer(modifier = Modifier.height(6.dp))
                              Text(
                                  text = if (isAr) opt.ar else opt.en,
                                  color = if (isSel) CyberCyan else Color.White.copy(alpha = 0.85f),
                                  fontSize = 10.sp, fontFamily = cascadiaCode, textAlign = TextAlign.Center,
                                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                              )
                          }
                      }
                  }
                  if (row.size == 1) Box(modifier = Modifier.weight(1f))
              }
          }
      }
  }

  @Composable
  private fun CalStepGoal(isAr: Boolean, opts: List<GoalOpt>, selectedKey: String, onSelect: (String) -> Unit) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("\uD83D\uDE80", fontSize = 52.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = if (isAr) "\u0645\u0627 \u0647\u0648 \u0647\u062f\u0641\u0643\u061f" else "What is your goal?",
              color = CyberCyan, fontSize = 26.sp, fontFamily = cascadiaCode, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
          )
          Text(
              text = if (isAr) "\u0633\u064a\u0648\u062c\u0651\u0647 \u0647\u062f\u0641\u0643 \u0645\u062d\u062a\u0648\u0649 \u062a\u062f\u0631\u064a\u0628\u0643" else "Your goal guides your training content",
              color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, fontFamily = cascadiaCode, textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
          )
          opts.forEach { opt ->
              val isSel = selectedKey == opt.key
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSel) CyberCyan.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                      .border(if (isSel) 2.dp else 1.dp, if (isSel) CyberCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                      .clickable { onSelect(opt.key) }
                      .padding(horizontal = 16.dp, vertical = 14.dp)
              ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(opt.icon, fontSize = 26.sp)
                      Spacer(modifier = Modifier.width(12.dp))
                      Text(
                          text = if (isAr) opt.ar else opt.en,
                          color = if (isSel) CyberCyan else Color.White.copy(alpha = 0.85f),
                          fontSize = 14.sp, fontFamily = cascadiaCode,
                          fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                          modifier = Modifier.weight(1f)
                      )
                      if (isSel) Text("\u2713", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                  }
              }
          }
      }
  }

  @Composable
  fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
      val bg = if (selected) CyberCyan else Color.Transparent
      val tc = if (selected) Color.Black else Color.White
      val bc = if (selected) CyberCyan else Color.White
      Box(
          modifier = Modifier
              .padding(4.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, bc, RoundedCornerShape(16.dp))
              .background(bg)
              .clickable(onClick = onClick)
              .padding(horizontal = 16.dp, vertical = 8.dp)
      ) { Text(text = text, color = tc, fontFamily = cascadiaCode, fontSize = 14.sp) }
  }
  