package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardViewModel
import org.json.JSONObject
import org.json.JSONArray

data class LibraryItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val body: String,
    val tag: String
)

@Composable
fun LibraryScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val isAr by viewModel.isArabic.collectAsState()
    val context = LocalContext.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Load data once
    val allData = remember(context) { loadLibraryData(context) }

    val tabs = if (isAr)
        listOf("الأدعية" to "duas", "الأحاديث" to "hadiths", "نصائح أمنية" to "security_tips")
    else
        listOf("Duas" to "duas", "Hadiths" to "hadiths", "Security Tips" to "security_tips")

    var selectedTab by remember { mutableStateOf("duas") }
    var searchQuery by remember { mutableStateOf("") }

    val displayedItems = remember(selectedTab, searchQuery, allData) {
        val items = allData[selectedTab] ?: emptyList()
        if (searchQuery.isBlank()) items
        else items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.body.contains(searchQuery, ignoreCase = true) ||
            it.tag.contains(searchQuery, ignoreCase = true)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lib_glow")
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "phase"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "pulse"
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF001020), Color(0xFF030810), VoidBlack), radius = 2000f)
        )) {
            // Ambient background orbs
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(brush = Brush.radialGradient(listOf(CyberCyan.copy(alpha = 0.04f), Color.Transparent), radius = size.width * 0.5f, center = Offset(size.width * 0.2f, size.height * 0.15f)), radius = size.width * 0.5f, center = Offset(size.width * 0.2f, size.height * 0.15f))
                drawCircle(brush = Brush.radialGradient(listOf(AmberGold.copy(alpha = 0.03f), Color.Transparent), radius = size.width * 0.4f, center = Offset(size.width * 0.8f, size.height * 0.7f)), radius = size.width * 0.4f, center = Offset(size.width * 0.8f, size.height * 0.7f))
                drawCircle(brush = Brush.radialGradient(listOf(PurpleVibe.copy(alpha = 0.03f), Color.Transparent), radius = size.width * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f)), radius = size.width * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            }

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                // ── Header ────────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().drawBehind {
                    val strokeW = 1.dp.toPx()
                    // Animated glow line at bottom of header
                    val phase = borderPhase
                    val lineY = size.height - strokeW / 2
                    drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, CyberCyan.copy(alpha = glowPulse), AmberGold.copy(alpha = glowPulse * 0.6f), Color.Transparent)), start = Offset(0f, lineY), end = Offset(size.width, lineY), strokeWidth = strokeW * 2)
                }.background(Brush.verticalGradient(listOf(Color(0xFF060D18), Color(0xFF030810)))).padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(CyberCyan.copy(alpha = 0.08f), CircleShape).border(1.dp, CyberCyan.copy(alpha = 0.25f * glowPulse + 0.15f), CircleShape).clickable { onBack() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAr) "المكتبة الرقمية" else "Digital Library",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "أدعية · أحاديث · أمن رقمي" else "Duas · Hadiths · Cybersecurity",
                                color = CyberCyan.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        // Item count badge
                        Box(modifier = Modifier.background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("${displayedItems.size}", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // ── Search Bar ─────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (isAr) "بحث..." else "Search...", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = CyberCyan.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (searchQuery.isNotEmpty()) { { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)) } } } else null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color(0xFF050D18),
                            unfocusedContainerColor = Color(0xFF030810),
                            cursorColor = CyberCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ── Tab Row ────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { (label, key) ->
                        val isSelected = selectedTab == key
                        val tabColor = when (key) {
                            "duas" -> AmberGold
                            "hadiths" -> NeonGreen
                            else -> CyberCyan
                        }
                        val tabGlow by infiniteTransition.animateFloat(
                            initialValue = if (isSelected) 0.3f else 0f, targetValue = if (isSelected) 0.6f else 0f,
                            animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "tabGlow_$key"
                        )
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) tabColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                                .border(if (isSelected) 1.dp else 0.5.dp, if (isSelected) tabColor.copy(alpha = 0.6f + tabGlow) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { selectedTab = key; searchQuery = "" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSelected) tabColor else Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // ── Items List ─────────────────────────────────────────
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (displayedItems.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text(if (isAr) "لا توجد نتائج" else "No results found", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        items(displayedItems, key = { "${selectedTab}_${it.id}" }) { item ->
                            LibraryItemCard(item = item, category = selectedTab, isAr = isAr, glowPulse = glowPulse)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItemCard(item: LibraryItem, category: String, isAr: Boolean, glowPulse: Float) {
    val accentColor = when (category) {
        "duas" -> AmberGold
        "hadiths" -> NeonGreen
        else -> CyberCyan
    }
    val icon = when (category) {
        "duas" -> "🤲"
        "hadiths" -> "📖"
        else -> "🛡️"
    }
    var expanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "card_${item.id}")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000 + item.id % 1500, easing = LinearEasing)), label = "shimmer"
    )

    Column(modifier = Modifier.fillMaxWidth()
        .drawBehind {
            // Subtle animated glow border
            val phase = shimmer
            val borderW = 0.8.dp.toPx()
            drawRoundRect(color = accentColor.copy(alpha = glowPulse * 0.25f), style = Stroke(borderW), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
            // Moving light spot
            val perimeter = 2 * (size.width + size.height)
            val spotPos = (phase * perimeter) % perimeter
            val (spotX, spotY) = when {
                spotPos < size.width -> spotPos to 0f
                spotPos < size.width + size.height -> size.width to (spotPos - size.width)
                spotPos < 2 * size.width + size.height -> (size.width - (spotPos - size.width - size.height)) to size.height
                else -> 0f to (size.height - (spotPos - 2 * size.width - size.height))
            }
            drawCircle(brush = Brush.radialGradient(listOf(accentColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.2f), Color.Transparent), center = Offset(spotX, spotY), radius = 30.dp.toPx()), radius = 30.dp.toPx(), center = Offset(spotX, spotY))
        }
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.05f), Color(0xFF030810))))
        .clickable { expanded = !expanded }
        .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Icon circle
            Box(modifier = Modifier.size(36.dp).background(accentColor.copy(alpha = 0.12f), CircleShape).border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 16.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
                if (item.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(item.subtitle, color = accentColor.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
            Spacer(Modifier.height(10.dp))
            Text(item.body, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 22.sp, textAlign = if (isAr) TextAlign.Right else TextAlign.Start)
            if (item.tag.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp)).border(0.5.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(item.tag, color = accentColor.copy(alpha = 0.8f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

fun loadLibraryData(context: Context): Map<String, List<LibraryItem>> {
    return try {
        val json = JSONObject(context.assets.open("sovereign_knowledge.json").bufferedReader().use { it.readText() })
        mapOf(
            "duas" to parseDuas(json.optJSONArray("duas")),
            "hadiths" to parseHadiths(json.optJSONArray("hadiths")),
            "security_tips" to parseSecurityTips(json.optJSONArray("security_tips"))
        )
    } catch (e: Exception) {
        mapOf("duas" to emptyList(), "hadiths" to emptyList(), "security_tips" to emptyList())
    }
}

private fun parseDuas(arr: JSONArray?): List<LibraryItem> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        // JSON keys: arabic, transliteration, occasion, source, category
        val arabic   = obj.optString("arabic", "")
        val occasion = obj.optString("occasion", obj.optString("category", "دعاء"))
        val source   = obj.optString("source", "")
        LibraryItem(
            id = i,
            title = occasion.ifBlank { "دعاء" },
            subtitle = if (source.isNotBlank()) "المصدر: $source" else "",
            body = arabic,                       // Arabic text only — no transliteration
            tag = "دعاء مأثور"
        )
    }
}

private fun parseHadiths(arr: JSONArray?): List<LibraryItem> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        // JSON keys: arabic, narrator, grade, topic
        val arabic   = obj.optString("arabic", "")
        val narrator = obj.optString("narrator", "")
        val topic    = obj.optString("topic", "")
        LibraryItem(
            id = i,
            title = if (topic.isNotBlank()) topic else arabic.take(50),
            subtitle = if (narrator.isNotBlank()) "رواه $narrator" else "",
            body = arabic,
            tag = obj.optString("grade", "")
        )
    }
}

// Boilerplate suffix appended to every Arabic tip in the dataset — strip it
// so tips don't look duplicated.
private val TIP_BOILERPLATE_AR = Regex(
    "\\s*هذا توجيه حيوي للحفاظ على وضعك الأمني الرقمي في ظل تهديدات متصاعدة\\.?\\s*$"
)
private val TIP_NUMBER_TAG_AR  = Regex("\\s*\\[نصيحة\\s*#?\\d+\\]\\s*")

private fun cleanTip(text: String): String =
    text.replace(TIP_NUMBER_TAG_AR, " ")
        .replace(TIP_BOILERPLATE_AR, "")
        .trim()

private fun parseSecurityTips(arr: JSONArray?): List<LibraryItem> {
    if (arr == null) return emptyList()
    val seen = HashSet<String>()
    val out  = mutableListOf<LibraryItem>()
    for (i in 0 until arr.length()) {
        val obj  = arr.getJSONObject(i)
        val body = cleanTip(obj.optString("tip_ar", obj.optString("tip_en", "")))
        if (body.isBlank()) continue
        // De-duplicate identical tips (after boilerplate stripping)
        val key = body.take(80)
        if (!seen.add(key)) continue
        out += LibraryItem(
            id = i,
            title = body.take(60).let { if (body.length > 60) "$it…" else it },
            subtitle = obj.optString("category_ar", obj.optString("category_en", "")),
            body = body,
            tag = obj.optString("category_en", "Security")
        )
    }
    return out
}
