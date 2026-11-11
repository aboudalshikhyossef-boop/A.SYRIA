package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CyberGlossaryData
import com.example.data.CyberTerm
import com.example.ui.design.AyriaDesignTokens
import com.example.ui.design.GlassCard
import com.example.ui.design.GlassChip
import com.example.ui.design.GlassErrorState
import com.example.ui.design.GlassLevel
import com.example.ui.design.GlassModalBottomSheet
import com.example.ui.design.GlassSurface
import com.example.ui.design.SectionHeader
import com.example.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

@Composable
fun CyberGlossaryScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val isAr by viewModel.isArabic.collectAsState()
    val haptic = LocalHapticFeedback.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("all") }
    var selectedTerm by remember { mutableStateOf<CyberTerm?>(null) }

    BackHandler {
        if (selectedTerm != null) selectedTerm = null else onBack()
    }

    val allCategories = remember(isAr) {
        listOf("all") + CyberGlossaryData.categories
    }
    val filteredTerms = remember(searchQuery, selectedCategory) {
        CyberGlossaryData.terms.filter { term ->
            val matchesSearch = searchQuery.isBlank() ||
                term.termAr.contains(searchQuery, ignoreCase = true) ||
                term.termEn.contains(searchQuery, ignoreCase = true) ||
                term.definitionAr.contains(searchQuery, ignoreCase = true) ||
                term.definitionEn.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "all" || term.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }
    val groupedTerms = remember(filteredTerms, isAr) {
        filteredTerms
            .sortedBy { (if (isAr) it.termAr else it.termEn).firstOrNull()?.uppercaseChar() ?: '#' }
            .groupBy { (if (isAr) it.termAr else it.termEn).firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            .toSortedMap()
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 136.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlossaryEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isAr) "قاموس الأمن السيبراني" else "CYBER GLOSSARY", color = AyriaDesignTokens.Colors.pearl, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text(if (isAr) "مصطلحات واضحة لفهم العالم الرقمي" else "Clear definitions for the language of digital security.", color = AyriaDesignTokens.Colors.muted, fontSize = 12.sp)
                    }
                    GlassSurface(
                        modifier = Modifier.size(44.dp).testTag("glossary-back").clickable(onClick = onBack),
                        shape = AyriaDesignTokens.Corners.pill,
                        level = GlassLevel.Standard
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isAr) "رجوع" else "Back", tint = AyriaDesignTokens.Colors.glacier)
                        }
                    }
                }
            }

            GlossaryEntrance(index = 1) {
                GlassSurface(modifier = Modifier.fillMaxWidth(), level = GlassLevel.Standard) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = AyriaDesignTokens.Colors.glacier)
                        Box(Modifier.weight(1f)) {
                            if (searchQuery.isBlank()) Text(if (isAr) "ابحث عن مصطلح..." else "Search terms...", color = AyriaDesignTokens.Colors.muted, fontSize = 12.sp)
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = AyriaDesignTokens.Colors.pearl, fontSize = 13.sp),
                                cursorBrush = SolidColor(AyriaDesignTokens.Colors.glacier),
                                modifier = Modifier.fillMaxWidth().testTag("glossary-search")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = AyriaDesignTokens.Colors.muted)
                            }
                        }
                    }
                }
            }

            GlossaryEntrance(index = 2) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allCategories.forEach { category ->
                        val selected = selectedCategory == category
                        val label = if (category == "all") {
                            if (isAr) "الكل" else "All"
                        } else category
                        Box(
                            modifier = Modifier.clip(AyriaDesignTokens.Corners.pill)
                                .background(if (selected) Brush.horizontalGradient(listOf(Color(0xFF24B9D5), Color(0xFF5969D8))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                        ) {
                            GlassChip(
                                label = label,
                                selected = selected,
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    selectedCategory = category
                                },
                                modifier = Modifier.testTag("glossary-category-$category")
                            )
                        }
                    }
                }
            }

            GlossaryEntrance(index = 3) {
                SectionHeader(
                    title = if (isAr) "المصطلحات" else "TERMS",
                    subtitle = if (isAr) "${filteredTerms.size} مصطلحاً · مرتّبة أبجدياً" else "${filteredTerms.size} terms · alphabetically grouped",
                    actionLabel = if (isAr) "عرض الكل" else "See all",
                    onAction = { selectedCategory = "all"; searchQuery = "" }
                )
            }

            when {
                CyberGlossaryData.terms.isEmpty() -> GlossaryEntrance(index = 4) {
                    GlassErrorState(
                        title = if (isAr) "القاموس غير متاح" else "Glossary unavailable",
                        message = if (isAr) "لم يتم تحميل قائمة المصطلحات المحلية." else "The local glossary did not provide any terms.",
                        retryLabel = if (isAr) "إعادة ضبط البحث" else "Reset filters",
                        onRetry = { searchQuery = ""; selectedCategory = "all" }
                    )
                }
                filteredTerms.isEmpty() -> GlossaryEntrance(index = 4) {
                    GlassCard(contentPadding = 8.dp) {
                        androidx.compose.material3.Icon(
                            Icons.Default.SearchOff, null,
                            tint = AyriaDesignTokens.Colors.glacier,
                            modifier = Modifier.size(28.dp).align(Alignment.CenterHorizontally)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        Text(
                            if (isAr) "لا توجد نتائج لـ «$searchQuery»" else "No results for “$searchQuery”",
                            color = AyriaDesignTokens.Colors.pearl,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            if (isAr) "جرّب مصطلحاً أو تصنيفاً آخر." else "Try another search term or category.",
                            color = AyriaDesignTokens.Colors.muted,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                else -> {
                    var entranceIndex = 4
                    groupedTerms.forEach { (letter, terms) ->
                        val sectionIndex = entranceIndex++
                        GlossaryEntrance(index = sectionIndex) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(34.dp).background(
                                        Brush.linearGradient(listOf(Color(0xFF25B9D4).copy(alpha = 0.28f), Color(0xFF6769D8).copy(alpha = 0.18f))),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(letter, color = AyriaDesignTokens.Colors.glacier, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(letter, color = AyriaDesignTokens.Colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = AyriaDesignTokens.Colors.line.copy(alpha = 0.48f))
                                Text("${terms.size}", color = AyriaDesignTokens.Colors.muted, fontSize = 10.sp)
                            }
                        }
                        terms.forEachIndexed { termIndex, term ->
                            val itemEntrance = entranceIndex++
                            GlossaryEntrance(index = itemEntrance) {
                                Column {
                                    GlossaryTermRow(
                                        term = term,
                                        isAr = isAr,
                                        onClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            selectedTerm = term
                                        }
                                    )
                                    if (termIndex < terms.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 18.dp, end = 12.dp),
                                            color = AyriaDesignTokens.Colors.line.copy(alpha = 0.32f),
                                            thickness = 0.7.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    GlassModalBottomSheet(
        visible = selectedTerm != null,
        title = selectedTerm?.let { if (isAr) it.termAr else it.termEn }.orEmpty(),
        subtitle = selectedTerm?.category,
        onDismissRequest = { selectedTerm = null }
    ) {
        selectedTerm?.let { term ->
            Column(Modifier.testTag("glossary-definition-sheet"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isAr) term.termEn else term.termAr,
                    color = AyriaDesignTokens.Colors.glacier,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(color = AyriaDesignTokens.Colors.line.copy(alpha = 0.56f))
                Text(
                    if (isAr) term.definitionAr else term.definitionEn,
                    color = AyriaDesignTokens.Colors.pearl,
                    fontSize = 14.sp,
                    lineHeight = 23.sp
                )
                Text(
                    if (isAr) term.definitionEn else term.definitionAr,
                    color = AyriaDesignTokens.Colors.muted,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun GlossaryTermRow(
    term: CyberTerm,
    isAr: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("glossary-term-${term.termEn}").clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(if (isAr) term.termAr else term.termEn, color = AyriaDesignTokens.Colors.pearl, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(if (isAr) term.termEn else term.termAr, color = AyriaDesignTokens.Colors.muted, fontSize = 10.sp, maxLines = 1)
        }
        Box(
            modifier = Modifier.clip(AyriaDesignTokens.Corners.pill)
                .background(categoryColor(term.category).copy(alpha = 0.12f))
                .border(0.7.dp, categoryColor(term.category).copy(alpha = 0.28f), AyriaDesignTokens.Corners.pill)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(term.category, color = categoryColor(term.category), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
        Text("›", color = AyriaDesignTokens.Colors.glacier, fontSize = 20.sp)
    }
}

private fun categoryColor(category: String): Color = when (category) {
    "هجمات" -> Color(0xFFFF6D7E)
    "برمجيات خبيثة" -> Color(0xFFFF9864)
    "تشفير" -> Color(0xFF9B8BFF)
    "مصادقة" -> Color(0xFF5ED6BC)
    "خصوصية" -> Color(0xFFFFC16F)
    "شبكات" -> Color(0xFF63CBE6)
    "دفاع" -> Color(0xFF73DFA8)
    "أمن الجوال" -> Color(0xFF78C7FF)
    "سحابة" -> Color(0xFF8AD8FF)
    "أخلاقيات" -> Color(0xFFFFD68A)
    else -> AyriaDesignTokens.Colors.glacier
}

@Composable
private fun GlossaryEntrance(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(65L + index.coerceAtMost(10) * 45L)
        visible = true
    }
    AnimatedVisibility(visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 14 }) {
        content()
    }
}
