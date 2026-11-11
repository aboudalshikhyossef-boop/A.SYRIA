package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.design.AyriaDesignTokens
import com.example.ui.design.GlassButton
import com.example.ui.design.GlassCard
import com.example.ui.design.GlassChip
import com.example.ui.design.GlassEmptyState
import com.example.ui.design.GlassLevel
import com.example.ui.design.GlassSurface
import com.example.ui.design.SectionHeader
import com.example.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

data class SecureResource(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val url: String,
    val icon: String,
    val color: Color,
    val category: String = "all"
)

@Composable
fun ResourcesScreen(
    viewModel: DashboardViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isAr by viewModel.isArabic.collectAsState()
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    var selectedCategory by rememberSaveable { mutableStateOf("all") }
    var isGrid by rememberSaveable { mutableStateOf(true) }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun openIntelligenceLink(url: String, title: String) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    val resources = remember {
        listOf(
            SecureResource(
                "فايروس توتال المتقدم", "VIRUSTOTAL SCANNER",
                "تحليل عميق للملفات والروابط والآي بي المشبوهة عبر محركات الفحص والذكاء الاصطناعي.",
                "Deep diagnostic analysis of files, URLs, and IPs against global antivirus engines.",
                "https://www.virustotal.com", "🛡️", Color(0xFFA8DDEB), "scan"
            ),
            SecureResource(
                "فاحص التسريبات الدولي", "HAVE I BEEN PWNED",
                "تحقق بشكل فوري ومستقل مما إذا كان بريدك الإلكتروني قد تسرب في اختراقات وقواعد بيانات عامة.",
                "Check if your personal credentials or emails have been leaked in historic data breaches.",
                "https://haveibeenpwned.com", "🔑", Color(0xFFB8DDE9), "leaks"
            ),
            SecureResource(
                "تقييم أمان المواقع", "URLVOID AUDIT",
                "تنقيب وفحص سمعة النطاقات والعناوين الإلكترونية لتفادي الوقوع في مصائد التصيد.",
                "Execute DNS reputation and domain security analysis to filter rogue or phishing urls.",
                "https://www.urlvoid.com", "🌐", Color(0xFFA8DDEB), "reputation"
            ),
            SecureResource(
                "معايير أمن الويب OWASP", "OWASP FOUNDATION",
                "الدليل والمنهج المفتوح الأكثر شهرة عالمياً للتعرف على أكبر ثغرات أمن التطبيقات السحابية.",
                "Direct link to standard awareness guidelines for top app vulnerabilities and web defense.",
                "https://owasp.org", "📚", Color(0xFFB8DDE9), "standards"
            )
        )
    }
    val categories = listOf(
        "all" to (if (isAr) "الكل" else "All"),
        "scan" to (if (isAr) "فحص الملفات" else "Scanners"),
        "leaks" to (if (isAr) "التسريبات" else "Breach checks"),
        "reputation" to (if (isAr) "سمعة المواقع" else "URL reputation"),
        "standards" to (if (isAr) "المعايير" else "Standards")
    )
    val visibleResources = resources.filter { selectedCategory == "all" || it.category == selectedCategory }
    val rows = visibleResources.chunked(if (isGrid) 2 else 1)

    BackHandler { onClose() }
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 136.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResourceEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isAr) "مصادر الاستخبارات" else "INTELLIGENCE RESOURCES", color = AyriaDesignTokens.Colors.pearl, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text(if (isAr) "أدوات موثوقة للتحقق والتحليل المفتوح OSINT" else "Trusted tools for open-source intelligence and verification.", color = AyriaDesignTokens.Colors.muted, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                    GlassButton(onClick = onClose, modifier = Modifier.widthIn(min = 66.dp).testTag("resources-back"), level = GlassLevel.Standard) {
                        Text(if (isAr) "رجوع" else "Back", color = AyriaDesignTokens.Colors.glacier, fontSize = 11.sp)
                    }
                }
            }

            ResourceEntrance(index = 1) {
                SectionHeader(
                    title = if (isAr) "اختر أداة" else "CHOOSE A TOOL",
                    subtitle = if (isAr) "مصادر خارجية — تفتح في المتصفح الآمن" else "External resources open in your browser",
                    actionLabel = if (isAr) "عرض الكل" else "See all",
                    onAction = { selectedCategory = "all" }
                )
            }

            ResourceEntrance(index = 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        categories.forEach { (key, label) ->
                            val selected = selectedCategory == key
                            Box(
                                modifier = Modifier.clip(AyriaDesignTokens.Corners.pill)
                                    .background(if (selected) Brush.horizontalGradient(listOf(Color(0xFF25B8D6), Color(0xFF5B69D9))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                            ) {
                                GlassChip(label, selected, { selectedCategory = key }, modifier = Modifier.testTag("resources-category-$key"))
                            }
                        }
                    }
                    IconButton(onClick = { isGrid = !isGrid }, modifier = Modifier.testTag("resources-view-toggle")) {
                        Icon(
                            if (isGrid) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGrid) "List view" else "Grid view",
                            tint = AyriaDesignTokens.Colors.glacier
                        )
                    }
                }
            }

            ResourceEntrance(index = 3) {
                Text(
                    if (isAr) "${visibleResources.size} مصادر متاحة" else "${visibleResources.size} resources available",
                    color = AyriaDesignTokens.Colors.muted,
                    fontSize = 11.sp
                )
            }

            if (visibleResources.isEmpty()) {
                GlassEmptyState(
                    icon = Icons.Default.OpenInNew,
                    title = if (isAr) "لا توجد أدوات في هذا التصنيف" else "No resources in this category",
                    description = if (isAr) "اختر تصنيفاً آخر لعرض الأدوات." else "Choose another category to view available tools."
                )
            } else {
                rows.forEachIndexed { index, rowItems ->
                    ResourceEntrance(index = 4 + index) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { resource ->
                                ResourceCard(
                                    resource = resource,
                                    isAr = isAr,
                                    isFavorite = resource.url in favorites,
                                    modifier = Modifier.weight(1f),
                                    onFavorite = {
                                        favorites = if (resource.url in favorites) favorites - resource.url else favorites + resource.url
                                    },
                                    onOpen = { openIntelligenceLink(resource.url, if (isAr) resource.titleAr else resource.titleEn) }
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

@Composable
private fun ResourceCard(
    resource: SecureResource,
    isAr: Boolean,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    val heartScale by animateFloatAsState(if (isFavorite) 1.18f else 1f, label = "resource-heart")
    val title = if (isAr) resource.titleAr else resource.titleEn
    val description = if (isAr) resource.descriptionAr else resource.descriptionEn
    GlassCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 242.dp).testTag("resource-card-${resource.category}")
            .animateContentSize().clickable(onClick = onOpen),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(58.dp).background(
                    Brush.linearGradient(
                        listOf(resource.color.copy(alpha = 0.30f), Color(0xFF6A72D8).copy(alpha = 0.20f))
                    ),
                    RoundedCornerShape(19.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Text(resource.icon, fontSize = 28.sp) }
            IconButton(onClick = onFavorite, modifier = Modifier.scale(heartScale).testTag("resource-favorite-${resource.category}")) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFF7EA8) else resource.color
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = resource.color, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, fontFamily = if (isAr) AyriaArabicFont else androidx.compose.ui.text.font.FontFamily.Monospace)
        Spacer(Modifier.height(6.dp))
        Text(description, color = AyriaDesignTokens.Colors.muted, fontSize = 10.sp, lineHeight = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(10.dp))
        Text(resource.url.removePrefix("https://"), color = AyriaDesignTokens.Colors.glacier.copy(alpha = 0.78f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.OpenInNew, null, tint = resource.color, modifier = Modifier.size(14.dp))
            Text(if (isAr) "زيارة الرابط" else "OPEN RESOURCE", color = resource.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResourceEntrance(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(70L + index * 60L)
        visible = true
    }
    AnimatedVisibility(visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 12 }) {
        content()
    }
}
