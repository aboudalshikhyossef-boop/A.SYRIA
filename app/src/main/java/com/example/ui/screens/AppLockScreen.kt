package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.example.ui.brand.AyriaActionButton
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaBrandMark
import com.example.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

private val PinAccent = Color(0xFF43D9F6)
private val PinAccentSecondary = Color(0xFF547BFF)
private val PinDanger = Color(0xFFFF5277)

@Composable
fun AppPinSettingsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val isEnabled by viewModel.isAppPinEnabled.collectAsState()
    val hasPin by viewModel.hasAppPin.collectAsState()
    var mode by remember(isEnabled, hasPin) {
        mutableStateOf(if (isEnabled && hasPin) "overview" else "disabled")
    }
    var pendingPin by remember { mutableStateOf("") }
    var firstPinForConfirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(PinAccent.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text(if (isArabic) "رجوع" else "Back", color = PinAccent)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (isArabic) "مركز قفل التطبيق" else "APP LOCK CONTROL",
                        color = PinAccent.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(22.dp))
                PinShieldMark()
                Spacer(Modifier.height(18.dp))

                when (mode) {
                    "overview" -> {
                        Text(
                            if (isArabic) "قفل A.SYRIA المحلي" else "A.SYRIA LOCAL LOCK",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isArabic) "القفل مفعّل. رمزك مخصص لهذا التطبيق ولا يرتبط برمز الهاتف." else "Protection is enabled. This PIN belongs only to this app and is independent of your phone PIN.",
                            color = Color.White.copy(alpha = 0.58f),
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(26.dp))
                        StatusPanel(
                            title = if (isArabic) "حالة الحماية" else "PROTECTION STATUS",
                            value = if (isArabic) "نشط ومشفّر محلياً" else "ACTIVE · ENCRYPTED LOCALLY"
                        )
                        Spacer(Modifier.height(14.dp))
                        ActionPanel(
                            title = if (isArabic) "تغيير رمز PIN" else "Change app PIN",
                            subtitle = if (isArabic) "تحقق من الرمز الحالي أولاً" else "Verify the current PIN first",
                            onClick = { error = null; mode = "verify_change" }
                        )
                        Spacer(Modifier.height(10.dp))
                        ActionPanel(
                            title = if (isArabic) "قفل التطبيق الآن" else "Lock app now",
                            subtitle = if (isArabic) "أغلق الجلسة واطلب PIN عند العودة" else "Lock this session and require the app PIN",
                            onClick = { viewModel.lockAppSession(); onBack() }
                        )
                        Spacer(Modifier.height(10.dp))
                        ActionPanel(
                            title = if (isArabic) "إيقاف قفل التطبيق" else "Disable app lock",
                            subtitle = if (isArabic) "سيُطلب الرمز الحالي قبل الإيقاف" else "Your current PIN is required",
                            danger = true,
                            onClick = { error = null; mode = "verify_disable" }
                        )
                        Spacer(Modifier.height(20.dp))
                        RecoveryNote(isArabic)
                    }

                    "disabled" -> {
                        Text(
                            if (isArabic) "حماية اختيارية، بتحكم كامل" else "OPTIONAL · UNDER YOUR CONTROL",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (isArabic) "أنشئ رمز PIN خاصاً من 6 أرقام. لن تظهر نافذة بصمة أو رمز الهاتف عند تشغيل التطبيق؛ سيظهر هذا القفل فقط بعد تفعيله." else "Create a private six-digit PIN. No biometric or phone-credential prompt will appear at startup; this in-app lock appears only after you enable it.",
                            color = Color.White.copy(alpha = 0.58f),
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(22.dp))
                        Button(
                            onClick = { pendingPin = ""; error = null; mode = "create" },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PinAccent.copy(alpha = 0.18f))
                        ) {
                            Icon(Icons.Default.Lock, null, tint = PinAccent)
                            Spacer(Modifier.width(10.dp))
                            Text(if (isArabic) "إنشاء رمز PIN وتفعيل القفل" else "Create PIN and enable lock", color = Color.White)
                        }
                        Spacer(Modifier.height(18.dp))
                        RecoveryNote(isArabic)
                    }

                    "verify_change", "verify_disable" -> {
                        val disabling = mode == "verify_disable"
                        PinEntryPanel(
                            title = if (isArabic) "أدخل الرمز الحالي" else "ENTER CURRENT PIN",
                            subtitle = if (isArabic) "تحقق مطلوب لحماية إعدادات القفل" else "Verification is required to protect lock settings",
                            isArabic = isArabic,
                            pin = pendingPin,
                            onPinChange = { pendingPin = it; error = null },
                            error = error,
                            actionLabel = if (isArabic) "متابعة" else "Continue",
                            actionEnabled = pendingPin.length == 6 && !isSaving,
                            onAction = {
                                isSaving = true
                                viewModel.verifyAppPin(pendingPin) { valid ->
                                    isSaving = false
                                    pendingPin = ""
                                    if (valid) {
                                        if (disabling) {
                                            viewModel.disableAppPin { disabled ->
                                                if (disabled) mode = "disabled"
                                                else error = if (isArabic) "تعذّر تحديث إعداد القفل. حاول مجدداً." else "Could not update lock settings. Please retry."
                                            }
                                        } else {
                                            firstPinForConfirmation = ""
                                            mode = "create"
                                            error = null
                                        }
                                    } else {
                                        error = if (isArabic) "الرمز غير صحيح أو أن مهلة المحاولات ما زالت فعّالة." else "The PIN is incorrect or the retry cooldown is active."
                                    }
                                }
                            },
                            secondaryLabel = if (isArabic) "إلغاء" else "Cancel",
                            onSecondary = { pendingPin = ""; error = null; mode = "overview" }
                        )
                    }

                    "create", "confirm" -> {
                        val confirming = mode == "confirm"
                        PinEntryPanel(
                            title = when {
                                confirming && isArabic -> "أكد الرمز الجديد"
                                confirming -> "CONFIRM NEW PIN"
                                isArabic -> "أنشئ رمزك الخاص"
                                else -> "CREATE YOUR APP PIN"
                            },
                            subtitle = if (confirming) {
                                if (isArabic) "أعد إدخال الأرقام الستة للتأكيد" else "Enter the same six digits again"
                            } else {
                                if (isArabic) "اختر ستة أرقام لا تستخدمها في كلمات مرور أخرى" else "Choose six digits that you do not reuse elsewhere"
                            },
                            isArabic = isArabic,
                            pin = pendingPin,
                            onPinChange = { pendingPin = it; error = null },
                            error = error,
                            actionLabel = if (isArabic) "تأكيد" else "Continue",
                            actionEnabled = pendingPin.length == 6 && !isSaving,
                            onAction = {
                                if (!confirming) {
                                    firstPinForConfirmation = pendingPin
                                    pendingPin = ""
                                    error = null
                                    mode = "confirm"
                                } else {
                                    // First entry is stored in the temporary state before the confirmation keypad.
                                    val firstEntry = firstPinForConfirmation
                                    if (pendingPin == firstEntry) {
                                        isSaving = true
                                        viewModel.enableAppPin(pendingPin) { saved ->
                                            isSaving = false
                                            if (saved) {
                                                pendingPin = ""
                                                firstPinForConfirmation = ""
                                                mode = "overview"
                                            } else {
                                                error = if (isArabic) "تعذّر حفظ الرمز بأمان. لم يتم تفعيل القفل." else "The PIN could not be stored securely. The lock was not enabled."
                                            }
                                        }
                                    } else {
                                        pendingPin = ""
                                        firstPinForConfirmation = ""
                                        error = if (isArabic) "الرمزان غير متطابقين. ابدأ الإنشاء من جديد." else "The PINs do not match. Start again."
                                        mode = "create"
                                    }
                                }
                            },
                            secondaryLabel = if (isArabic) "رجوع" else "Back",
                            onSecondary = {
                                pendingPin = ""
                                error = null
                                if (confirming) { firstPinForConfirmation = ""; mode = "create" } else mode = if (isEnabled) "overview" else "disabled"
                            }
                        )
                    }
                }
                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
fun AppPinLockScreen(
    isArabic: Boolean,
    failedAttempts: Int,
    lockoutUntilMillis: Long,
    recoveryMode: Boolean,
    onVerify: (String, (Boolean) -> Unit) -> Unit,
    onForgotPin: () -> Unit,
    onResetPin: (String, (Boolean) -> Unit) -> Unit
) {
    var pin by remember(recoveryMode) { mutableStateOf("") }
    var firstRecoveryPin by remember(recoveryMode) { mutableStateOf("") }
    var recoveryConfirm by remember(recoveryMode) { mutableStateOf(false) }
    var error by remember(recoveryMode) { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lockoutUntilMillis) {
        while (lockoutUntilMillis > System.currentTimeMillis()) {
            now = System.currentTimeMillis()
            delay(200)
        }
        now = System.currentTimeMillis()
    }

    val remainingMillis = (lockoutUntilMillis - now).coerceAtLeast(0L)
    val cooldownActive = remainingMillis > 0L
    val cooldownSeconds = ((remainingMillis + 999L) / 1000L).toInt()
    val cooldownProgress = (remainingMillis / 30_000f).coerceIn(0f, 1f)

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF020817), Color(0xFF071A35), Color(0xFF020612))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(Brush.verticalGradient(listOf(PinAccent.copy(alpha = 0.22f), Color.Transparent)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PinShieldMark()
                Spacer(Modifier.height(22.dp))

                if (!recoveryMode) {
                    Text(
                        "A.SYRIA",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (isArabic) "مساحة آمنة. وصول خاص." else "A SECURE SPACE. YOUR PRIVATE ACCESS.",
                        color = PinAccent.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    PinEntryPanel(
                        title = if (isArabic) "أدخل رمز التطبيق" else "ENTER APP PIN",
                        subtitle = if (isArabic) "أدخل الرمز المكوّن من ستة أرقام للمتابعة" else "Enter your six-digit app PIN to continue",
                        isArabic = isArabic,
                        pin = pin,
                        onPinChange = { pin = it; error = null },
                        error = error ?: if (failedAttempts in 1..4) {
                            if (isArabic) "رمز غير صحيح · تبقّت ${5 - failedAttempts} محاولات" else "Incorrect PIN · ${5 - failedAttempts} attempts remain"
                        } else null,
                        actionLabel = if (isArabic) "فتح التطبيق" else "Unlock A.SYRIA",
                        actionEnabled = pin.length == 6 && !cooldownActive && !busy,
                        onAction = {
                            busy = true
                            onVerify(pin) { valid ->
                                busy = false
                                if (valid) {
                                    pin = ""
                                    error = null
                                } else {
                                    pin = ""
                                    error = if (isArabic) "رمز PIN غير صحيح." else "Incorrect app PIN."
                                }
                            }
                        }
                    )

                    AnimatedVisibility(visible = cooldownActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.045f))
                                .border(1.dp, PinDanger.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = PinDanger, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isArabic) "تم إيقاف المحاولات مؤقتاً" else "RETRY TEMPORARILY PAUSED",
                                    color = PinDanger,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isArabic) "يمكنك إعادة المحاولة بعد $cooldownSeconds ثانية" else "You can retry in $cooldownSeconds seconds",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { cooldownProgress },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                                color = PinAccent,
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )
                        }
                    }

                    AnimatedVisibility(visible = failedAttempts >= 5) {
                        TextButton(
                            onClick = onForgotPin,
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = AmberZen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isArabic) "نسيت رمز التطبيق؟ استرد الوصول" else "Forgot app PIN? Recover access",
                                color = AmberZen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        if (isArabic) "استرداد الوصول" else "ACCESS RECOVERY",
                        color = AmberZen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isArabic) "تم التحقق من هوية الجهاز. أنشئ الآن رمزاً جديداً من ستة أرقام." else "Device identity verified. Create a new six-digit app PIN.",
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    PinEntryPanel(
                        title = if (recoveryConfirm) {
                            if (isArabic) "أكد الرمز الجديد" else "CONFIRM NEW PIN"
                        } else {
                            if (isArabic) "أنشئ رمزاً جديداً" else "CREATE A NEW PIN"
                        },
                        subtitle = if (recoveryConfirm) {
                            if (isArabic) "أعد إدخال الرمز نفسه" else "Enter the same PIN again"
                        } else {
                            if (isArabic) "لن يتغير رمز هاتفك؛ هذا الرمز خاص بالتطبيق" else "Your phone PIN is unchanged; this one belongs only to the app"
                        },
                        isArabic = isArabic,
                        pin = pin,
                        onPinChange = { pin = it; error = null },
                        error = error,
                        actionLabel = if (isArabic) "تأكيد الرمز" else "Confirm PIN",
                        actionEnabled = pin.length == 6 && !busy,
                        onAction = {
                            if (!recoveryConfirm) {
                                firstRecoveryPin = pin
                                pin = ""
                                recoveryConfirm = true
                                error = null
                            } else if (pin == firstRecoveryPin) {
                                busy = true
                                onResetPin(pin) { saved ->
                                    busy = false
                                    if (saved) {
                                        pin = ""
                                        firstRecoveryPin = ""
                                        error = null
                                    } else {
                                        pin = ""
                                        error = if (isArabic) "تعذّر حفظ الرمز الجديد بأمان. حاول مجدداً." else "The new PIN could not be saved securely. Please retry."
                                    }
                                }
                            } else {
                                pin = ""
                                firstRecoveryPin = ""
                                recoveryConfirm = false
                                error = if (isArabic) "الرمزان غير متطابقين. ابدأ من جديد." else "The PINs do not match. Start again."
                            }
                        },
                        secondaryLabel = if (recoveryConfirm) {
                            if (isArabic) "رجوع" else "Back"
                        } else null,
                        onSecondary = {
                            pin = ""
                            firstRecoveryPin = ""
                            recoveryConfirm = false
                            error = null
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    if (isArabic) "حماية محلية • تشفير AES-GCM • لا يتم إرسال الرمز إلى أي خادم" else "LOCAL PROTECTION • AES-GCM ENCRYPTION • YOUR PIN IS NEVER SENT TO A SERVER",
                    color = Color.White.copy(alpha = 0.30f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PinEntryPanel(
    title: String,
    subtitle: String,
    isArabic: Boolean,
    pin: String,
    onPinChange: (String) -> Unit,
    error: String?,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.075f), Color(0xFF16324A).copy(alpha = 0.25f), Color.White.copy(alpha = 0.025f))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(PinAccent.copy(alpha = 0.42f), Color.White.copy(alpha = 0.10f), PinAccentSecondary.copy(alpha = 0.30f))),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Default,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(7.dp))
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.52f),
            fontSize = 11.sp,
            fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Default,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val filled = index < pin.length
                val ringScale by animateFloatAsState(
                    targetValue = if (filled) 1.07f else 1f,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "pin_indicator_scale_$index"
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .graphicsLayer { scaleX = ringScale; scaleY = ringScale }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (filled) PinAccent.copy(alpha = 0.28f) else Color(0xFF173354).copy(alpha = 0.42f),
                                    Color(0xFF07182D).copy(alpha = 0.68f),
                                    Color.White.copy(alpha = 0.025f)
                                )
                            ),
                            CircleShape
                        )
                        .border(
                            1.2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.82f),
                                    PinAccent.copy(alpha = if (filled) 0.95f else 0.58f),
                                    PinAccentSecondary.copy(alpha = 0.72f),
                                    Color.White.copy(alpha = 0.30f),
                                    Color.White.copy(alpha = 0.82f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(if (filled) 9.dp else 4.dp)
                            .background(
                                if (filled) Color(0xFFEAFBFF) else Color.White.copy(alpha = 0.30f),
                                CircleShape
                            )
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(width = 15.dp, height = 2.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (filled) 0.78f else 0.30f))
                    )
                }
            }
        }

        AnimatedVisibility(visible = !error.isNullOrBlank()) {
            Text(
                error.orEmpty(),
                modifier = Modifier.padding(top = 12.dp),
                color = PinDanger,
                fontSize = 10.sp,
                fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Default,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(18.dp))
        val keypadRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "back")
        )
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(Modifier.size(66.dp))
                    } else {
                        val interaction = remember { MutableInteractionSource() }
                        val pressed by interaction.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (pressed) 0.91f else 1f,
                            animationSpec = tween(125, easing = FastOutSlowInEasing),
                            label = "pin_key_press_$digit"
                        )
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF2B75AD).copy(alpha = 0.34f),
                                            Color(0xFF10345D).copy(alpha = 0.62f),
                                            Color(0xFF06152A).copy(alpha = 0.94f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(
                                    1.3.dp,
                                    Brush.sweepGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.88f),
                                            PinAccent.copy(alpha = 0.78f),
                                            PinAccentSecondary.copy(alpha = 0.80f),
                                            Color.White.copy(alpha = 0.20f),
                                            Color.White.copy(alpha = 0.88f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable(
                                    enabled = pin.length < 6 || digit == "back",
                                    interactionSource = interaction,
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (digit == "back") onPinChange(pin.dropLast(1))
                                    else if (pin.length < 6) onPinChange(pin + digit)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .size(55.dp)
                                    .border(0.7.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            )
                            if (digit == "back") {
                                Icon(
                                    Icons.Default.Backspace,
                                    contentDescription = if (isArabic) "حذف" else "Delete",
                                    tint = PinAccent,
                                    modifier = Modifier.size(21.dp)
                                )
                            } else {
                                Text(
                                    digit,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 9.dp)
                                    .size(width = 22.dp, height = 3.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.68f))
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        AyriaActionButton(
            text = actionLabel,
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = actionEnabled
        )
        if (secondaryLabel != null && onSecondary != null) {
            TextButton(onClick = onSecondary, modifier = Modifier.padding(top = 3.dp)) {
                Text(secondaryLabel, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontFamily = if (isArabic) AyriaArabicFont else FontFamily.Default)
            }
        }
    }
}

@Composable
private fun PinShieldMark() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF276DAB).copy(alpha = 0.36f), Color(0xFF0A1D3C).copy(alpha = 0.94f))
                )
            )
            .border(
                1.2.dp,
                Brush.sweepGradient(
                    listOf(Color.White.copy(alpha = 0.78f), PinAccent, PinAccentSecondary, Color.White.copy(alpha = 0.22f))
                ),
                RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AyriaBrandMark(
            modifier = Modifier.size(84.dp),
            contentDescription = "A.SYRIA"
        )
    }
}

@Composable
private fun StatusPanel(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, PinAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(15.dp)
    ) {
        Text(title, color = PinAccent.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF5DF2B4), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActionPanel(
    title: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val accent = if (danger) PinDanger else PinAccent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.065f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (danger) Icons.Default.LockOpen else Icons.Default.Lock, null, tint = accent, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun RecoveryNote(isArabic: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberZen.copy(alpha = 0.055f))
            .border(1.dp, AmberZen.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Fingerprint, null, tint = AmberZen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        Text(
            if (isArabic) "لاستعادة الرمز المنسي، استخدم بصمة أو PIN أو نمط أو كلمة مرور الهاتف. أضف قفل شاشة آمناً إلى هاتفك حتى تبقى وسيلة الاسترداد متاحة." else "To recover a forgotten PIN, use your phone's biometric or screen credential. Keep a secure screen lock enabled so recovery remains available.",
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 10.sp,
            lineHeight = 16.sp
        )
    }
}
