package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

data class ChecklistItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val priority: String,
    val frequency: String
)

@Composable
fun SecurityChecklistScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val isAr by viewModel.isArabic.collectAsState()
    val haptic = LocalHapticFeedback.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    var checkedItems by remember { mutableStateOf(setOf<String>()) }
    var selectedFrequency by remember { mutableStateOf("يومي") }

    BackHandler { onBack() }

    val allItems = remember {
        listOf(
            ChecklistItem("pw_change", "تحديث كلمات المرور", "Update Passwords",
                "قم بتغيير كلمة مرور الحسابات الحساسة بشكل منتظم واستخدم كلمات مرور قوية ومعقدة.",
                "Change passwords for sensitive accounts regularly and use strong, complex passwords.",
                "عالي", "شهري"),
            ChecklistItem("2fa_check", "التحقق من المصادقة الثنائية", "Verify 2FA Status",
                "تأكد من تفعيل المصادقة الثنائية على جميع الحسابات المهمة: البريد، والبنك، والتواصل الاجتماعي.",
                "Ensure 2FA is enabled on all important accounts: email, banking, and social media.",
                "عالي", "أسبوعي"),
            ChecklistItem("updates", "تحديث النظام والتطبيقات", "System & App Updates",
                "ثبّت آخر تحديثات الأمان لنظام التشغيل وجميع التطبيقات على الفور.",
                "Install the latest security updates for your operating system and all applications immediately.",
                "عالي", "يومي"),
            ChecklistItem("backup", "نسخ احتياطي للبيانات", "Data Backup",
                "تأكد من أن نسخك الاحتياطية محدّثة ومحمية بتشفير قوي في مكان آمن.",
                "Ensure your backups are current and protected with strong encryption in a safe location.",
                "عالي", "أسبوعي"),
            ChecklistItem("suspicious_links", "فحص الروابط المشبوهة", "Check Suspicious Links",
                "لا تنقر على أي رابط مشبوه في الرسائل أو البريد الإلكتروني دون التحقق من مصدره أولاً.",
                "Never click suspicious links in messages or emails without first verifying their source.",
                "عالي", "يومي"),
            ChecklistItem("wifi_check", "مراجعة اتصالات Wi-Fi", "Review Wi-Fi Connections",
                "افصل عن الشبكات العامة غير الآمنة واستخدم VPN عند الاتصال بشبكات غير معروفة.",
                "Disconnect from unsecured public networks and use VPN when connecting to unknown networks.",
                "متوسط", "يومي"),
            ChecklistItem("app_permissions", "مراجعة صلاحيات التطبيقات", "Review App Permissions",
                "راجع صلاحيات التطبيقات بانتظام وأزل الوصول غير الضروري للكاميرا والميكروفون والموقع.",
                "Regularly review app permissions and remove unnecessary access to camera, microphone, and location.",
                "متوسط", "شهري"),
            ChecklistItem("phishing_vigilance", "اليقظة من التصيد", "Phishing Vigilance",
                "تحقق من عنوان المرسل وإشارات التحذير في كل رسالة بريد إلكتروني مشبوهة قبل التفاعل معها.",
                "Verify sender address and warning signs in every suspicious email before interacting with it.",
                "عالي", "يومي"),
            ChecklistItem("screen_lock", "قفل الشاشة", "Screen Lock",
                "تأكد من تفعيل قفل الشاشة ببصمة أو PIN معقد وضبط التأمين التلقائي بعد فترة قصيرة.",
                "Ensure screen lock is enabled with fingerprint or complex PIN, and auto-lock is set to a short interval.",
                "عالي", "يومي"),
            ChecklistItem("antivirus", "فحص مكافحة الفيروسات", "Antivirus Scan",
                "شغّل فحصاً شاملاً لمكافحة الفيروسات بشكل دوري وتأكد من تحديث قاعدة بيانات التهديدات.",
                "Run a full antivirus scan regularly and ensure the threat database is up to date.",
                "متوسط", "أسبوعي"),
            ChecklistItem("account_review", "مراجعة نشاط الحسابات", "Account Activity Review",
                "راجع سجل تسجيل الدخول في حساباتك وابحث عن أي نشاط غير مألوف أو دخول من مواقع مجهولة.",
                "Review login history in your accounts and look for any unusual activity or logins from unknown locations.",
                "متوسط", "أسبوعي"),
            ChecklistItem("social_privacy", "مراجعة إعدادات الخصوصية", "Privacy Settings Review",
                "راجع إعدادات الخصوصية على منصات التواصل الاجتماعي وقلّل المعلومات المكشوفة للعموم.",
                "Review privacy settings on social media platforms and minimize publicly exposed information.",
                "منخفض", "شهري"),
            ChecklistItem("old_accounts", "حذف الحسابات القديمة", "Delete Old Accounts",
                "احذف الحسابات على الخدمات التي لم تعد تستخدمها لتقليل سطح الهجوم على بياناتك.",
                "Delete accounts on services you no longer use to reduce the attack surface on your data.",
                "منخفض", "شهري"),
            ChecklistItem("encryption", "فحص تشفير البيانات", "Data Encryption Check",
                "تأكد من تشفير المجلدات الحساسة على هاتفك وجهاز الحاسوب الخاص بك.",
                "Ensure sensitive folders on your phone and computer are encrypted.",
                "متوسط", "شهري"),
            ChecklistItem("email_alias", "استخدام عناوين بريد بديلة", "Use Email Aliases",
                "استخدم عناوين بريد إلكتروني مستعارة عند التسجيل في خدمات غير موثوقة لحماية بريدك الأصلي.",
                "Use email aliases when registering with untrusted services to protect your real email address.",
                "منخفض", "شهري"),
        )
    }

    val frequencies = if (isAr) listOf("يومي", "أسبوعي", "شهري") else listOf("Daily", "Weekly", "Monthly")

    val filtered = remember(selectedFrequency, isAr) {
        allItems.filter {
            if (isAr) it.frequency == selectedFrequency
            else when (selectedFrequency) {
                "Daily" -> it.frequency == "يومي"
                "Weekly" -> it.frequency == "أسبوعي"
                "Monthly" -> it.frequency == "شهري"
                else -> true
            }
        }
    }

    val completedCount = filtered.count { it.id in checkedItems }
    val progress = if (filtered.isEmpty()) 0f else completedCount.toFloat() / filtered.size

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        0.3f, 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "g"
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CyberCyan)
                        }
                        Text(
                            text = if (isAr) "قائمة الأمان السيادية" else "SOVEREIGN CHECKLIST",
                            color = CyberCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .border(0.5.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .background(CyberCyan.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isAr) "التقدم الأمني" else "SECURITY PROGRESS",
                                    color = CyberCyan.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$completedCount / ${filtered.size}",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = if (progress >= 0.8f) CyberCyan else AmberZen,
                                trackColor = CyberCyan.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isAr) {
                                    when {
                                        progress >= 1f -> "✅ مستوى الحماية: ممتاز"
                                        progress >= 0.7f -> "🔰 مستوى الحماية: جيد"
                                        progress >= 0.4f -> "⚠️ مستوى الحماية: متوسط"
                                        else -> "🚨 مستوى الحماية: ضعيف"
                                    }
                                } else {
                                    when {
                                        progress >= 1f -> "✅ Protection Level: EXCELLENT"
                                        progress >= 0.7f -> "🔰 Protection Level: GOOD"
                                        progress >= 0.4f -> "⚠️ Protection Level: MODERATE"
                                        else -> "🚨 Protection Level: WEAK"
                                    }
                                },
                                color = if (progress >= 0.8f) CyberCyan else AmberZen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequencies.forEach { freq ->
                            val selected = freq == selectedFrequency
                            Box(
                                modifier = Modifier.weight(1f)
                                    .border(
                                        1.dp,
                                        if (selected) CyberCyan else CyberCyan.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (selected) CyberCyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedFrequency = freq
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    color = if (selected) CyberCyan else Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                items(filtered, key = { it.id }) { item ->
                    val isChecked = item.id in checkedItems
                    val priorityColor = when (item.priority) {
                        "عالي" -> Color(0xFFFF4444)
                        "متوسط" -> AmberZen
                        else -> CyberCyan.copy(alpha = 0.6f)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .border(
                                0.5.dp,
                                if (isChecked) CyberCyan.copy(alpha = 0.4f) else priorityColor.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .background(
                                if (isChecked) CyberCyan.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                checkedItems = if (isChecked) checkedItems - item.id else checkedItems + item.id
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isChecked) CyberCyan else priorityColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp).padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isAr) item.titleAr else item.titleEn,
                                        color = if (isChecked) CyberCyan else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.alpha(if (isChecked) 0.7f else 1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .border(0.5.dp, priorityColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.priority,
                                            color = priorityColor,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isAr) item.descAr else item.descEn,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.alpha(if (isChecked) 0.5f else 1f)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    if (progress >= 1f) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .border(1.dp, CyberCyan.copy(alpha = glowAlpha), RoundedCornerShape(12.dp))
                                .background(CyberCyan.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "✅",
                                    fontSize = 32.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (isAr) "أحسنت! تم استيفاء جميع متطلبات الأمان" else "EXCELLENT! All security requirements fulfilled",
                                    color = CyberCyan,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isAr) "وَٱللَّهُ خَيۡرٌ حَٰفِظٗا" else "SOVEREIGN SHIELD: ACTIVE",
                                    color = AmberZen.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
