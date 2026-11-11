package com.example.ui.screens

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.ui.design.GlassErrorState
import com.example.ui.design.GlassLoadingState
import com.example.ui.design.SectionHeader
import com.example.ui.design.GlassButton
import com.example.ui.design.GlassSurface
import com.example.ui.design.GlassModalBottomSheet
import com.example.ui.design.GlassLevel
import com.example.ui.design.GlassChip
import com.example.ui.design.GlassCard
import com.example.ui.design.AyriaDesignTokens
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.*
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
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    var allData by remember(context) { mutableStateOf<Map<String, List<LibraryItem>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf("duas") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isGrid by rememberSaveable { mutableStateOf(true) }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    var readItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedItem by remember { mutableStateOf<LibraryItem?>(null) }

    fun reloadLibrary() {
        scope.launch {
            isLoading = true
            allData = withContext(Dispatchers.IO) { loadLibraryData(context) }
            isLoading = false
        }
    }
    LaunchedEffect(context) { reloadLibrary() }

    val displayedItems = remember(selectedTab, searchQuery, allData) {
        val source = allData[selectedTab].orEmpty()
        if (searchQuery.isBlank()) source
        else source.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
                item.body.contains(searchQuery, ignoreCase = true) ||
                item.tag.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }
    val libraryKey = { item: LibraryItem -> "${selectedTab}_${item.id}" }
    val gridRows = remember(displayedItems, isGrid) {
        displayedItems.chunked(if (isGrid) 2 else 1)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 136.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LibraryEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isAr) "مكتبة السكينة" else "QUIET LIBRARY", color = AyriaDesignTokens.Colors.pearl, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (isAr) "أدعية وأحاديث ومواقيت الصلاة" else "Duas, hadiths and daily prayer times",
                            color = AyriaDesignTokens.Colors.muted, fontSize = 12.sp
                        )
                    }
                    GlassButton(onClick = onBack, modifier = Modifier.widthIn(min = 66.dp).testTag("library-back"), level = GlassLevel.Standard) {
                        Text(if (isAr) "رجوع" else "Back", color = AyriaDesignTokens.Colors.glacier, fontSize = 11.sp)
                    }
                }
            }

            LibraryEntrance(index = 1) {
                GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isAr) "مواقيت الصلاة" else "PRAYER TIMES", color = AyriaDesignTokens.Colors.glacier, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(
                                if (isAr) "الصلاة القادمة · $nextPrayerName" else "NEXT · $nextPrayerName",
                                color = AyriaDesignTokens.Colors.muted, fontSize = 10.sp
                            )
                            Text(nextPrayerTime, color = AyriaDesignTokens.Colors.pearl, fontSize = 28.sp, fontWeight = FontWeight.Light)
                            Text(nextPrayerCountdown, color = AyriaDesignTokens.Colors.glacier, fontSize = 12.sp)
                        }
                        PrayerCountdownWheel(
                            prayerName = nextPrayerName,
                            prayerTime = nextPrayerTime,
                            countdown = nextPrayerCountdown,
                            progress = nextPrayerProgress,
                            isArabic = isAr,
                            diameter = 104.dp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    DeskPrayerSchedule(
                        prayerTimes = prayerTimes,
                        nextPrayer = nextPrayerName,
                        isArabic = isAr,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            LibraryEntrance(index = 2) {
                LibrarySearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = if (isAr) "ابحث في الأدعية والأحاديث..." else "Search duas and hadiths...",
                    onClear = { searchQuery = "" }
                )
            }

            LibraryEntrance(index = 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "duas" to (if (isAr) "الأدعية" else "Duas"),
                        "hadiths" to (if (isAr) "الأحاديث" else "Hadiths")
                    ).forEach { (key, label) ->
                        Box(
                            modifier = Modifier.clip(AyriaDesignTokens.Corners.pill)
                                .background(if (selectedTab == key) Brush.horizontalGradient(listOf(Color(0xFF28B8D4), Color(0xFF546DDB))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                        ) {
                            GlassChip(label, selectedTab == key, {
                                selectedTab = key
                                searchQuery = ""
                            }, modifier = Modifier.testTag("library-tab-$key"))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { isGrid = !isGrid }, modifier = Modifier.testTag("library-view-toggle")) {
                        Icon(
                            if (isGrid) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGrid) "List view" else "Grid view",
                            tint = AyriaDesignTokens.Colors.glacier
                        )
                    }
                }
            }

            LibraryEntrance(index = 4) {
                SectionHeader(
                    title = if (isAr) "المحتوى" else "LIBRARY CONTENT",
                    subtitle = if (isAr) "${displayedItems.size} عنصر متاح" else "${displayedItems.size} entries available",
                    actionLabel = if (isAr) "عرض الكل" else "See all",
                    onAction = { searchQuery = "" }
                )
            }

            when {
                isLoading -> LibraryEntrance(index = 5) {
                    GlassLoadingState(
                        title = if (isAr) "جارٍ تحميل المكتبة" else "Loading your library",
                        description = if (isAr) "يتم تجهيز الأدعية والأحاديث المحلية." else "Preparing the offline duas and hadith collection."
                    )
                }
                allData.values.sumOf { it.size } == 0 -> LibraryEntrance(index = 5) {
                    GlassErrorState(
                        title = if (isAr) "تعذّر تحميل محتوى المكتبة" else "Library content unavailable",
                        message = if (isAr) "تعذّر العثور على عناصر في ملف المكتبة المحلي." else "The local library asset did not return any entries.",
                        retryLabel = if (isAr) "إعادة المحاولة" else "Retry",
                        onRetry = { reloadLibrary() }
                    )
                }
                displayedItems.isEmpty() -> LibraryEntrance(index = 5) {
                    GlassEmptyState(
                        icon = Icons.Default.SearchOff,
                        title = if (isAr) "لا توجد نتائج مطابقة" else "No matching entries",
                        description = if (isAr) "جرّب تغيير عبارة البحث أو اختيار قسم آخر." else "Try another search or choose the other category."
                    )
                }
                else -> {
                    gridRows.forEachIndexed { index, rowItems ->
                        LibraryEntrance(index = 5 + index) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    LibraryItemCard(
                                        item = item,
                                        category = selectedTab,
                                        isAr = isAr,
                                        isFavorite = libraryKey(item) in favorites,
                                        isRead = libraryKey(item) in readItems,
                                        modifier = Modifier.weight(1f).testTag("library-item-${libraryKey(item)}"),
                                        onFavorite = {
                                            val key = libraryKey(item)
                                            favorites = if (key in favorites) favorites - key else favorites + key
                                        },
                                        onClick = {
                                            readItems = readItems + libraryKey(item)
                                            selectedItem = item
                                        }
                                    )
                                }
                                if (isGrid && rowItems.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    GlassModalBottomSheet(
        visible = selectedItem != null,
        title = selectedItem?.title.orEmpty(),
        subtitle = selectedItem?.subtitle?.takeIf { it.isNotBlank() },
        onDismissRequest = { selectedItem = null }
    ) {
        selectedItem?.let { item ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    item.body,
                    color = AyriaDesignTokens.Colors.pearl,
                    fontSize = 19.sp,
                    lineHeight = 34.sp,
                    textAlign = if (isAr) TextAlign.Right else TextAlign.Start,
                    fontFamily = AyriaArabicFont
                )
                HorizontalDivider(color = AyriaDesignTokens.Colors.line.copy(alpha = 0.6f))
                if (item.tag.isNotBlank()) Text(item.tag, color = AyriaDesignTokens.Colors.glacier, fontSize = 11.sp)
                if (item.reference.isNotBlank()) Text(item.reference, color = AyriaDesignTokens.Colors.muted, fontSize = 11.sp)
                if (item.sourceUrl.isNotBlank()) {
                    GlassButton(
                        onClick = { runCatching { uriHandler.openUri(item.sourceUrl) } },
                        modifier = Modifier.fillMaxWidth().testTag("library-open-source")
                    ) {
                        Icon(Icons.Default.OpenInNew, null, tint = AyriaDesignTokens.Colors.glacier, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAr) "فتح المصدر والتحقق" else "Open source & verify", color = AyriaDesignTokens.Colors.glacier)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun LibrarySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onClear: () -> Unit
) {
    GlassSurface(modifier = Modifier.fillMaxWidth().testTag("library-search"), level = GlassLevel.Standard) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = AyriaDesignTokens.Colors.glacier)
            Box(Modifier.weight(1f)) {
                if (value.isBlank()) Text(placeholder, color = AyriaDesignTokens.Colors.muted, fontSize = 12.sp)
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AyriaDesignTokens.Colors.pearl, fontSize = 13.sp),
                    cursorBrush = SolidColor(AyriaDesignTokens.Colors.glacier),
                    modifier = Modifier.fillMaxWidth().testTag("library-search-input")
                )
            }
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.testTag("library-search-clear")) {
                    Icon(Icons.Default.Close, null, tint = AyriaDesignTokens.Colors.muted)
                }
            }
        }
    }
}

@Composable
private fun LibraryEntrance(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(70L + index * 60L)
        visible = true
    }
    AnimatedVisibility(visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 12 }) {
        content()
    }
}

@Composable
fun LibraryItemCard(
    item: LibraryItem,
    category: String,
    isAr: Boolean,
    isFavorite: Boolean = false,
    isRead: Boolean = false,
    modifier: Modifier = Modifier,
    onFavorite: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val heartScale by animateFloatAsState(if (isFavorite) 1.18f else 1f, label = "library-heart")
    val accent = AyriaDesignTokens.Colors.glacier
    GlassCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 192.dp).testTag("library-item")
            .clickable(onClick = onClick),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(
                    Brush.linearGradient(listOf(Color(0xFF2BB9D1).copy(alpha = 0.28f), Color(0xFF7A68E8).copy(alpha = 0.20f))),
                    RoundedCornerShape(17.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Text(if (category == "duas") "🤲" else "📖", fontSize = 26.sp) }
            IconButton(onClick = onFavorite, modifier = Modifier.scale(heartScale).testTag("library-favorite")) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFF7EA8) else accent
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(item.title, color = AyriaDesignTokens.Colors.pearl, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(
            item.subtitle.ifBlank { item.tag },
            color = AyriaDesignTokens.Colors.muted,
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
        if (isRead) {
            LinearProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                color = AyriaDesignTokens.Colors.teal,
                trackColor = AyriaDesignTokens.Colors.line
            )
        } else {
            Text(if (isAr) "غير مقروء · اضغط للقراءة" else "Not read · Tap to open", color = accent.copy(alpha = 0.8f), fontSize = 9.sp)
        }
        Spacer(Modifier.height(7.dp))
        Text(if (isAr) "اقرأ التفاصيل  ↗" else "READ DETAILS  ↗", color = AyriaDesignTokens.Colors.teal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
