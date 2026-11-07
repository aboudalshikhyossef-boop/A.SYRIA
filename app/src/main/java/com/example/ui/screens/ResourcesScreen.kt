package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

data class SecureResource(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val url: String,
    val icon: String,
    val color: Color
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

    val resources = listOf(
        SecureResource(
            titleAr = "فايروس توتال المتقدم",
            titleEn = "VIRUSTOTAL SCANNER",
            descriptionAr = "تحليل عميق للملفات والروابط والآي بي المشبوهة عبر محركات الفحص والذكاء الاصطناعي.",
            descriptionEn = "Deep diagnostic analysis of files, URLs, and IPs against global antivirus engines.",
            url = "https://www.virustotal.com",
            icon = "🛡",
            color = CyberCyan
        ),
        SecureResource(
            titleAr = "فاحص التسريبات الدولي",
            titleEn = "HAVE I BEEN PWNED",
            descriptionAr = "تحقق بشكل فوري ومستقل مما إذا كان بريدك الإلكتروني قد تسرب في اختراقات وقواعد بيانات عامة.",
            descriptionEn = "Check if your personal credentials or emails have been leaked in historic data breaches.",
            url = "https://haveibeenpwned.com",
            icon = "🔑",
            color = AmberZen
        ),
        SecureResource(
            titleAr = "تقييم أمان المواقع",
            titleEn = "URLVOID AUDIT",
            descriptionAr = "تنقيب وفحص سمعة النطاقات والعناوين الإلكترونية لتفادي الوقوع في مصائد التصيد.",
            descriptionEn = "Execute DNS reputation and domain security analysis to filter rogue or phishing urls.",
            url = "https://www.urlvoid.com",
            icon = "🌐",
            color = CyberCyan
        ),
        SecureResource(
            titleAr = "معايير أمن الويب OWASP",
            titleEn = "OWASP FOUNDATION",
            descriptionAr = "الدليل والمنهج المفتوح الأكثر شهرة عالمياً للتعرف على أكبر ثغرات أمن التطبيقات السحابية.",
            descriptionEn = "Direct link to standard awareness guidelines for top app vulnerabilities and web defense.",
            url = "https://owasp.org",
            icon = "📚",
            color = AmberZen
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VoidBlack, Color(0xFF04070D))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "مصادر الاستخبارات" else "INTELLIGENCE NODES",
                            color = AmberZen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isAr) "بروتوكولات الاستخبارات المفتوحة OSINT" else "OPEN SOURCE INTELLIGENCE HUB",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Back button target tag with 48dp touch target
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(1.dp, AmberZen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onClose()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAr) "رجوع" else "BACK",
                            color = AmberZen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Intro Text Section
                Text(
                    text = if (isAr) {
                        "قم بتشغيل خطوط الاتصال المباشرة بقواعد تحليل مخاطر الشبكة الدولية المعترفة لتقييم سلامة الكيانات السحابية الرقمية."
                    } else {
                        "Verify digital assets directly. Touch any matrix node to establish a high-security visual hyperlink mapping in your default terminal browser."
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Professional Responsive Grid Layout
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resources) { resource ->
                        val title = if (isAr) resource.titleAr else resource.titleEn
                        val description = if (isAr) resource.descriptionAr else resource.descriptionEn

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp) // Ensures consistent card sizing, avoiding layout shifting
                                .border(
                                    width = 0.5.dp,
                                    color = resource.color.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(Color(0xFF070B12).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    openIntelligenceLink(resource.url, title)
                                }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .border(0.5.dp, resource.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .background(resource.color.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = resource.icon,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Text(
                                        text = "UPLINK_//",
                                        color = resource.color.copy(alpha = 0.4f),
                                        fontSize = 7.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = title,
                                    color = resource.color,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = description,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Interactive anchor button
                            Text(
                                text = if (isAr) "زيارة الرابط ⮥" else "ESTABLISH LINK ⮥",
                                color = resource.color,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}
