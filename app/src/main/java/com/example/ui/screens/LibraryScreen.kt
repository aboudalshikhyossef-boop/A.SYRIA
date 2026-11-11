package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.design.GlassEmptyState
import org.json.JSONObject
import org.json.JSONArray

data class LibraryItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val body: String,
    val tag: String,
    val source: String = "",
    val reference: String = "",
    val sourceUrl: String = ""
)

@Composable
fun LibraryScreen(
    viewModel: DashboardViewModel,
    prayerViewModel: com.example.ui.viewmodel.PrayerViewModel,
    onBack: () -> Unit
) {
    val isAr by viewModel.isArabic.collectAsState()
    val nextPrayerName by prayerViewModel.nextPrayerName.collectAsState()
    val nextPrayerTime by prayerViewModel.nextPrayerTime.collectAsState()
    val nextPrayerCountdown by prayerViewModel.nextPrayerCountdown.collectAsState()
    val nextPrayerProgress by prayerViewModel.nextPrayerProgress.collectAsState()
    val prayerTimes by prayerViewModel.allPrayerTimes.collectAsState()
    val context = LocalContext.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    val allData = remember(context) { loadLibraryData(context) }
    val tabs = if (isAr) listOf("الأدعية" to "duas", "الأحاديث" to "hadiths")
        else listOf("Duas" to "duas", "Hadiths" to "hadiths")
    var selectedTab by rememberSaveable { mutableStateOf("duas") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val displayedItems = remember(selectedTab, searchQuery, allData) {
        val source = allData[selectedTab].orEmpty()
        if (searchQuery.isBlank()) source
        else source.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
                item.body.contains(searchQuery, ignoreCase = true) ||
                item.tag.contains(searchQuery, ignoreCase = true)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x268FC8DE), CircleShape)
                        .border(0.8.dp, Color(0x558DC9DE), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isAr) "رجوع" else "Back",
                        tint = Color(0xFFD7EEF6),
                        modifier = Modifier.size(19.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        if (isAr) "مكتبة السكينة" else "QUIET LIBRARY",
                        color = Color(0xFFE0F3F8),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace,
                        letterSpacing = if (isAr) 0.sp else 1.1.sp
                    )
                    Text(
                        if (isAr) "أدعية وأحاديث ومواقيت الصلاة" else "DUAS · HADITHS · PRAYER TIMES",
                        color = Color(0xFFA7C8D5).copy(alpha = 0.66f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.4.sp
                    )
                }
                Text(
                    displayedItems.size.toString(),
                    color = Color(0xFFC5E7F0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0x247BAFC5), CircleShape)
                        .border(0.7.dp, Color(0x447BAFC5), CircleShape)
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }

            // Prayer information remains visible in both Duas and Hadiths tabs.
            DeskFrostPanel(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (isAr) "مواقيت الصلاة" else "PRAYER TIMES",
                            color = Color(0xFFD6EEF5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace
                        )
                        Text(
                            if (isAr) "الصلاة القادمة · $nextPrayerName" else "NEXT · $nextPrayerName",
                            color = Color(0xFFB2D2DF).copy(alpha = 0.76f),
                            fontSize = 9.sp,
                            fontFamily = AyriaArabicFont
                        )
                        Text(
                            nextPrayerTime,
                            color = Color(0xFFE4F5F9),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            nextPrayerCountdown,
                            color = Color(0xFFC3E0EA),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.3.sp
                        )
                    }
                    PrayerCountdownWheel(
                        prayerName = nextPrayerName,
                        prayerTime = nextPrayerTime,
                        countdown = nextPrayerCountdown,
                        progress = nextPrayerProgress,
                        isArabic = isAr,
                        diameter = 116.dp
                    )
                }
                Spacer(Modifier.height(9.dp))
                DeskPrayerSchedule(
                    prayerTimes = prayerTimes,
                    nextPrayer = nextPrayerName,
                    isArabic = isAr,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isAr) "ابحث في الأدعية والأحاديث..." else "Search duas and hadiths...",
                        color = Color(0xFFA8C3D0).copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        fontFamily = if (isAr) AyriaArabicFont else FontFamily.Default
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color(0xFFB9DDE8), modifier = Modifier.size(18.dp))
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFB9DDE8).copy(alpha = 0.7f), modifier = Modifier.size(17.dp))
                        }
                    }
                } else null,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9DCDDE).copy(alpha = 0.68f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = Color(0x66162F43),
                    unfocusedContainerColor = Color(0x55162F43),
                    cursorColor = Color(0xFFC9EAF4),
                    focusedTextColor = Color(0xFFE8F5F9),
                    unfocusedTextColor = Color(0xFFE8F5F9)
                ),
                shape = RoundedCornerShape(17.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (label, key) ->
                    val selected = selectedTab == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Color(0x3A8FC8DE) else Color.White.copy(alpha = 0.035f))
                            .border(
                                if (selected) 0.9.dp else 0.6.dp,
                                if (selected) Color(0x779BD6E6) else Color.White.copy(alpha = 0.10f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedTab = key; searchQuery = "" }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color(0xFFE0F4FA) else Color(0xFFABC3CE).copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                if (displayedItems.isEmpty()) {
                    item {
                        GlassEmptyState(
                            icon = Icons.Default.SearchOff,
                            title = if (isAr) "لا توجد نتائج مطابقة" else "No matching entries",
                            description = if (isAr) "جرّب تغيير عبارة البحث أو اختيار قسم آخر." else "Try another search or choose a different category."
                        )
                    }
                } else {
                    items(displayedItems, key = { "${selectedTab}_${it.id}" }) { item ->
                        LibraryItemCard(item = item, category = selectedTab, isAr = isAr)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItemCard(item: LibraryItem, category: String, isAr: Boolean) {
    var expanded by rememberSaveable(item.id, category) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val accentColor = Color(0xFFB8DDE9)
    val cardShape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xCA1A394D), Color(0x9D10283B), Color(0x76182F42))
                ),
                cardShape
            )
            .border(
                0.8.dp,
                Brush.linearGradient(
                    listOf(Color(0x559BCFE0), Color(0x228BC5D8), Color(0x16FFFFFF))
                ),
                cardShape
            )
            .clickable { expanded = !expanded }
            .padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                modifier = Modifier
                    .size(39.dp)
                    .background(Color(0x278FC9DF), CircleShape)
                    .border(0.8.dp, Color(0x558FC9DF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (category == "duas") "🤲" else "📖", fontSize = 17.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.title,
                    color = Color(0xFFE4F4F8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                    fontFamily = if (isAr) AyriaArabicFont else FontFamily.Default
                )
                if (item.subtitle.isNotBlank()) {
                    Text(
                        item.subtitle,
                        color = accentColor.copy(alpha = 0.68f),
                        fontSize = 9.sp,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = accentColor.copy(alpha = 0.68f),
                modifier = Modifier.size(20.dp)
            )
        }
        if (expanded) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
            Spacer(Modifier.height(11.dp))
            Text(
                item.body,
                color = Color(0xFFE8F5F8),
                fontSize = if (category == "duas") 19.sp else 17.sp,
                lineHeight = if (category == "duas") 34.sp else 30.sp,
                textAlign = if (isAr) TextAlign.Right else TextAlign.Start,
                fontFamily = AyriaArabicFont
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.tag.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x1F9ACDDF), RoundedCornerShape(50))
                            .border(0.7.dp, Color(0x448FC9DF), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            item.tag,
                            color = Color(0xFFC8E5EE),
                            fontSize = 9.sp,
                            fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace
                        )
                    }
                }
                if (item.reference.isNotBlank()) {
                    Text(
                        item.reference,
                        modifier = Modifier.weight(1f, fill = false),
                        color = Color(0xFFA9C9D5).copy(alpha = 0.75f),
                        fontSize = 9.sp,
                        fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace
                    )
                }
            }
            if (item.sourceUrl.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { runCatching { uriHandler.openUri(item.sourceUrl) } },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFFB9DFEA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isAr) "فتح المصدر والتحقق" else "Open source & verify",
                        color = Color(0xFFB9DFEA),
                        fontSize = 10.sp,
                        fontFamily = if (isAr) AyriaArabicFont else FontFamily.Monospace
                    )
                }
            }
        }
    }
}

fun loadLibraryData(context: Context): Map<String, List<LibraryItem>> {
    return try {
        val json = JSONObject(
            context.assets.open("islamic_library.json")
                .bufferedReader()
                .use { it.readText() }
        )
        mapOf(
            "duas" to parseDuas(json.optJSONArray("duas")),
            "hadiths" to parseHadiths(json.optJSONArray("hadiths"))
        )
    } catch (error: Exception) {
        android.util.Log.e("A.SYRIA.Library", "Could not read the verified Islamic library asset", error)
        mapOf("duas" to emptyList(), "hadiths" to emptyList())
    }
}

private fun parseDuas(arr: JSONArray?): List<LibraryItem> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).mapNotNull { index ->
        val item = arr.optJSONObject(index) ?: return@mapNotNull null
        val arabic = item.optString("arabic").trim()
        if (arabic.isBlank()) return@mapNotNull null
        val source = item.optString("source").trim()
        val reference = item.optString("reference").trim()
        val occasion = item.optString("occasion", "دعاء").ifBlank { "دعاء" }

        LibraryItem(
            id = item.optInt("id", index),
            title = occasion,
            subtitle = listOf(source, reference).filter { it.isNotBlank() }.joinToString(" · "),
            body = arabic,
            tag = item.optString("grade").ifBlank {
                item.optString("kind").ifBlank { "دعاء" }
            },
            source = source,
            reference = reference,
            sourceUrl = item.optString("source_url").trim()
        )
    }.distinctBy { it.body to it.reference }
}

private fun parseHadiths(arr: JSONArray?): List<LibraryItem> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).mapNotNull { index ->
        val item = arr.optJSONObject(index) ?: return@mapNotNull null
        val arabic = item.optString("arabic").trim()
        if (arabic.isBlank()) return@mapNotNull null
        val narrator = item.optString("narrator").trim()
        val source = item.optString("source").trim()
        val reference = item.optString("reference").trim()
        val topic = item.optString("topic").ifBlank { arabic.take(48) }
        val meta = listOf(
            narrator.takeIf { it.isNotBlank() }?.let { "عن $it" },
            source.takeIf { it.isNotBlank() }
        ).filterNotNull().joinToString(" · ")

        LibraryItem(
            id = item.optInt("id", index),
            title = topic,
            subtitle = meta,
            body = arabic,
            tag = item.optString("grade").ifBlank { "لم تُسجَّل الدرجة" },
            source = source,
            reference = reference,
            sourceUrl = item.optString("source_url").trim()
        )
    }.distinctBy { it.body to it.reference }
}
