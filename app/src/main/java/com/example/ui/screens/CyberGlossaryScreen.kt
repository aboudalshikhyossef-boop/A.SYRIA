package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CyberGlossaryData
import com.example.data.CyberTerm
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun CyberGlossaryScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val isAr by viewModel.isArabic.collectAsState()
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var expandedTermId by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    val allCategories = remember(isAr) {
        val base = if (isAr) listOf("الكل") else listOf("All")
        base + CyberGlossaryData.categories
    }

    val filteredTerms = remember(searchQuery, selectedCategory) {
        CyberGlossaryData.terms.filter { term ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                term.termAr.contains(searchQuery, ignoreCase = true) ||
                term.termEn.contains(searchQuery, ignoreCase = true) ||
                term.definitionAr.contains(searchQuery, ignoreCase = true) ||
                term.definitionEn.contains(searchQuery, ignoreCase = true)
            }
            val matchesCategory = selectedCategory == "الكل" || selectedCategory == "All" ||
                    term.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        0.2f, 0.6f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "ga"
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFF020A14))
                            .padding(top = 16.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CyberCyan)
                                }
                                Text(
                                    text = if (isAr) "قاموس الأمن السيبراني" else "CYBER SECURITY GLOSSARY",
                                    color = CyberCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Box(
                                    modifier = Modifier
                                        .border(0.5.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .background(CyberCyan.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${filteredTerms.size}",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                placeholder = {
                                    Text(
                                        if (isAr) "ابحث عن مصطلح..." else "Search terms...",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = CyberCyan.copy(alpha = 0.6f)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, null, tint = Color.White.copy(alpha = 0.4f))
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = CyberCyan.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = CyberCyan
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                            )
                        }
                    }
                }
                item {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCategories) { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .border(
                                        0.8.dp,
                                        if (isSelected) CyberCyan else CyberCyan.copy(alpha = 0.2f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .background(
                                        if (isSelected) CyberCyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedCategory = cat
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) CyberCyan else Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                if (filteredTerms.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAr) "لا توجد نتائج لـ \"$searchQuery\"" else "No results for \"$searchQuery\"",
                                color = Color.White.copy(alpha = 0.3f),
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredTerms, key = { it.termEn }) { term ->
                        CyberTermCard(
                            term = term,
                            isAr = isAr,
                            isExpanded = expandedTermId == term.termEn,
                            glowAlpha = glowAlpha,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                expandedTermId = if (expandedTermId == term.termEn) null else term.termEn
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberTermCard(
    term: CyberTerm,
    isAr: Boolean,
    isExpanded: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    val categoryColor = when (term.category) {
        "هجمات" -> Color(0xFFFF4444)
        "برمجيات خبيثة" -> Color(0xFFFF6B35)
        "تشفير" -> Color(0xFF7B61FF)
        "مصادقة" -> Color(0xFF00BFA5)
        "خصوصية" -> Color(0xFFFF9800)
        "شبكات" -> CyberCyan
        "دفاع" -> Color(0xFF69F0AE)
        "أمن الجوال" -> Color(0xFF40C4FF)
        "سحابة" -> Color(0xFF80D8FF)
        "أخلاقيات" -> AmberZen
        else -> Color.White.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(
                width = if (isExpanded) 0.8.dp else 0.4.dp,
                color = if (isExpanded) categoryColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                if (isExpanded) categoryColor.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) term.termAr else term.termEn,
                        color = if (isExpanded) categoryColor else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isAr) {
                        Text(
                            text = term.termAr,
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = term.termEn,
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .border(0.5.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = term.category,
                        color = categoryColor,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            if (isExpanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = categoryColor.copy(alpha = 0.2f))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (isAr) term.definitionAr else term.definitionEn,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
                if (isAr) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "[ ${term.termEn} ]",
                        color = categoryColor.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
