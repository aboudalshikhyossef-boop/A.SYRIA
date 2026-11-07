package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.delay

data class AcademyScenario(
    val id: String,
    val scenario: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Immutable
data class AcademySyllabusModule(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val icon: String
)

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    width: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val xShimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xShimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            CyberCyan.copy(alpha = 0.05f),
            CyberCyan.copy(alpha = 0.2f),
            CyberCyan.copy(alpha = 0.05f),
        ),
        start = Offset(xShimmer - 300f, xShimmer - 300f),
        end = Offset(xShimmer, xShimmer)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(brush, RoundedCornerShape(4.dp))
    )
}

@Composable
fun AcademyScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
    onNavigateToGlossary: () -> Unit = {},
    onNavigateToChecklist: () -> Unit = {}
) {
    val isAr by viewModel.isArabic.collectAsState()
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    val haptic = LocalHapticFeedback.current

    var selectedModule by remember { mutableStateOf<AcademySyllabusModule?>(null) }

    val syllabusModules = remember {
        listOf(
            AcademySyllabusModule(
                id = "social_engineering",
                titleAr = "الهندسة الاجتماعية",
                titleEn = "Social Engineering",
                descAr = "طرق التلاعب بالبشر للحصول على معلومات بأساليب احتيال متقدمة.",
                descEn = "Manipulating humans into revealing confidential credentials with advanced psychological vectors.",
                icon = "👥"
            ),
            AcademySyllabusModule(
                id = "mobile_security",
                titleAr = "أمن الهواتف",
                titleEn = "Mobile Security",
                descAr = "حماية أنظمة iOS و Android من تطبيقات التجسس العميقة.",
                descEn = "Securing iOS and Android hosts against zero-click kernel spyware.",
                icon = "📱"
            ),
            AcademySyllabusModule(
                id = "network_safety",
                titleAr = "سلامة الشبكات",
                titleEn = "Network Safety",
                descAr = "إحباط هجمات اعتراض البيانات وهجمات الرجل في المنتصف الذكية.",
                descEn = "Detecting and disabling Wi-Fi interception vectors & Man-in-the-Middle payloads.",
                icon = "🌐"
            ),
            AcademySyllabusModule(
                id = "privacy_rights",
                titleAr = "حقوق الخصوصية",
                titleEn = "Privacy Rights",
                descAr = "الاستراتيجيات الرقمية والتشريعية لمقاومة التتبع ومسح البيانات.",
                descEn = "Tactics and legal models to prevent metadata aggregation & non-consensual scraping.",
                icon = "⚖"
            )
        )
    }

    BackHandler(enabled = selectedModule != null) {
        selectedModule = null
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VoidBlack, Color(0xFF04070D))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "الأكاديمية السيادية" else "SOVEREIGN ACADEMY",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isAr) "منهج الدفاع المتقدم والاستخبارات المهنية" else "ADVANCED DEFENSIVE & INTEL CURRICULUM",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Quick Language Toggle inside screen
                Box(
                    modifier = Modifier
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.setArabic(!isAr)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isAr) "EN" else "العربية",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Module view switching
            AnimatedContent(
                targetState = selectedModule,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { if (targetState != null) it else -it }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { if (targetState != null) -it else it }) + fadeOut()
                },
                label = "academyTransition"
            ) { module ->
                if (module == null) {
                    // Showcase syllabus modules
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isAr) "اختر ملف المنهج للبدء بالاختبار العصبي:" else "SELECT A SYLLABUS DIRECTORY FOR NEURAL TESTING:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        syllabusModules.forEach { item ->
                            val title = if (isAr) item.titleAr else item.titleEn
                            val desc = if (isAr) item.descAr else item.descEn

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(0.5.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedModule = item
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = item.icon,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title.uppercase(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    Text(
                                        text = "🡠",
                                        color = CyberCyan,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Active neural scenario test for selected syllabus module
                    NeuralModuleTestView(
                        module = module,
                        viewModel = viewModel,
                        isAr = isAr,
                        onBack = { selectedModule = null }
                    )
                }
            }
        }
    }
}

@Composable
fun NeuralModuleTestView(
    module: AcademySyllabusModule,
    viewModel: DashboardViewModel,
    isAr: Boolean,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isGeneratingScenarios by remember { mutableStateOf(false) }

    // Restore scenarios from ViewModel if same module (survives tab navigation)
    val savedJson = remember(module.id) { viewModel.getSavedAcademyScenariosJson() }
    val savedIdx = remember(module.id) { viewModel.getSavedAcademyIndex() }
    val savedIds = remember(module.id) { viewModel.getSavedAcademyUsedIds().toMutableSet() }
    val isSameModule = remember(module.id) { viewModel.getSavedAcademyModuleId() == module.id }

    fun parseJsonToScenarios(json: String): List<AcademyScenario> {
        if (json.isBlank()) return emptyList()
        return try {
            val root = JSONObject(json)
            val array = root.getJSONArray("scenarios")
            val list = mutableListOf<AcademyScenario>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(AcademyScenario(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    scenario = obj.getString("scenario"),
                    options = (0 until obj.getJSONArray("options").length()).map { obj.getJSONArray("options").getString(it) },
                    correctIndex = obj.getInt("correctIndex"),
                    explanation = obj.optString("explanation", "")
                ))
            }
            list
        } catch (e: Exception) { emptyList() }
    }

    fun scenariosToJson(list: List<AcademyScenario>): String {
        val arr = org.json.JSONArray()
        list.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("scenario", s.scenario)
            val opts = org.json.JSONArray()
            s.options.forEach { opts.put(it) }
            obj.put("options", opts)
            obj.put("correctIndex", s.correctIndex)
            obj.put("explanation", s.explanation)
            arr.put(obj)
        }
        return JSONObject().apply { put("scenarios", arr) }.toString()
    }

    var scenariosList by remember {
        mutableStateOf(if (isSameModule && savedJson.isNotBlank()) parseJsonToScenarios(savedJson) else emptyList())
    }
    var currentScenarioIndex by remember { mutableStateOf(if (isSameModule) savedIdx else 0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var debriefText by remember { mutableStateOf("") }
    var isGeneratingDebrief by remember { mutableStateOf(false) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }

    // Track used scenario IDs across all API calls to avoid repetition
    val usedScenarioIds = remember(module.id) {
        if (isSameModule) savedIds else mutableSetOf()
    }

    // Save progress to ViewModel whenever it changes
    LaunchedEffect(scenariosList, currentScenarioIndex, usedScenarioIds.size) {
        if (scenariosList.isNotEmpty()) {
            viewModel.saveAcademyProgress(
                moduleId = module.id,
                scenariosJson = scenariosToJson(scenariosList),
                index = currentScenarioIndex,
                usedIds = usedScenarioIds.toSet()
            )
        }
    }

    // Multi-Language high fidelity offline scenarios per module
    val offlineScenarios = remember(module.id) {
        when (module.id) {
            "social_engineering" -> listOf(
                AcademyScenario(
                    id = "off_se_1",
                    scenario = if (isAr) {
                        "تلقيت استدعاءً طارئاً يزعم أنه من فريق الدعم الفني لشركة Uber منتحلاً هوية مهندس مهاد فني، ويطلب الحصول على رمز التحقق للمصادقة الثنائية (MFA Fatigue) بزعم تجنب توقف حسابك."
                    } else {
                        "You receive successive, urgent push alerts from Uber support, followed by an SMS claiming a support specialist needs your Multi-Factor Authentication (MFA) validation code immediately to resolve a pending terminal error. What protocol should you execute?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) مشاركة الكود المستلم لتسهيل عملية الدعم الفني فورا.",
                            "ب) تجاهل الكود والانتظار حتى يتوقف التنبيه التلقائي.",
                            "ج) رفض إرسال الكود بالمطلق، وتأكيد المحاولة مع مركز الدعم الرسمي بشكل منعزل للتحقق من المخترق.",
                            "د) تقديم رمز مرور عشوائي لتشويش محاولات الطرف الطالب."
                        )
                    } else {
                        listOf(
                            "A) Grant the code to expedite validation and restore workflow.",
                            "B) Standby and hope the automated alerts expire without further vector damage.",
                            "C) Strictly reject giving any code, flag the session as unauthorized, and contact support through authenticated systems.",
                            "D) Provide a pseudo-random OTP sequence to confuse the tracking entity."
                        )
                    },
                    correctIndex = 2,
                    explanation = if (isAr) "دائما ارفض مشاركة رموز التحقق وتواصل عبر القنوات الرسمية." else "Always reject verification codes and use official channels."
                ),
                AcademyScenario(
                    id = "off_se_2",
                    scenario = if (isAr) {
                        "تلقيت بريداً إلكترونياً من عنوان يبدو رسمياً يدّعي أنه من مصرفك، يطلب منك النقر على رابط وإدخال بياناتك للتحقق من حسابك خلال 24 ساعة أو سيتم تجميد الحساب. ما إجراؤك؟"
                    } else {
                        "You receive an email from what appears to be your bank, claiming your account will be suspended in 24 hours unless you click a link and verify your credentials immediately. This is a classic phishing vector. What is your action?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) النقر على الرابط الفوري وإدخال بياناتك لتجنب تجميد الحساب.",
                            "ب) تمرير الرسالة لأصدقائك لمعرفة رأيهم.",
                            "ج) فتح متصفح جديد وكتابة عنوان بنكك مباشرة والتحقق من الحساب هناك، والإبلاغ عن البريد كتصيد.",
                            "د) الرد على البريد بطلب تفاصيل أكثر قبل اتخاذ أي إجراء."
                        )
                    } else {
                        listOf(
                            "A) Click the link immediately to prevent account suspension.",
                            "B) Forward it to a friend to get their opinion.",
                            "C) Open a new browser, type your bank's URL directly, verify your account there, and report the email as phishing.",
                            "D) Reply to the email requesting more details before taking action."
                        )
                    },
                    correctIndex = 2,
                    explanation = if (isAr) "الروابط في رسائل التصيد تقود إلى مواقع مزيفة. دائماً اكتب عنوان الموقع مباشرة." else "Phishing links lead to fake sites. Always type the URL directly and report suspicious emails."
                ),
                AcademyScenario(
                    id = "off_se_3",
                    scenario = if (isAr) {
                        "شخص يدّعي أنه من قسم تقنية المعلومات في شركتك يطلب منك كلمة مرورك عبر الهاتف لإجراء صيانة عاجلة على الخوادم، مهدداً بأن عملك سيتأثر إن لم تتعاون فوراً."
                    } else {
                        "A caller claiming to be from your company's IT department requests your password over the phone, threatening urgent server maintenance will fail and you'll be blamed. This is a classic vishing (voice phishing) attack. How do you respond?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) إعطاء كلمة المرور فوراً لتجنب المشاكل.",
                            "ب) إعطاء كلمة مرور قديمة بدلاً من الحالية.",
                            "ج) رفض الطلب بشكل قاطع، وإنهاء المكالمة، والإبلاغ عن الحادثة لقسم الأمن المختص.",
                            "د) طلب المتصل إرسال طلب رسمي عبر البريد أولاً ثم مشاركة المعلومات."
                        )
                    } else {
                        listOf(
                            "A) Provide the password immediately to avoid consequences.",
                            "B) Give an old password instead of the current one.",
                            "C) Firmly refuse, end the call, and report the incident to your security team immediately.",
                            "D) Ask them to send a formal email request first, then share credentials."
                        )
                    },
                    correctIndex = 2,
                    explanation = if (isAr) "لا يطلب أي قسم تقنية شرعي كلمة مرورك أبداً. ارفض دائماً وأبلغ فريق الأمن." else "No legitimate IT team ever asks for your password. Always refuse and report immediately."
                )
            )
            "mobile_security" -> listOf(
                AcademyScenario(
                    id = "off_ms_1",
                    scenario = if (isAr) {
                        "تلقيت تنبيهاً أمنياً غامضاً يحثك على تثبيت ملف تعريفي خارجي (Configuration Profile) على هاتفك مستغلاً هندسة أمنية لتثبيت برمجية تجسس نووية خبيثة تشبه Pegasus."
                    } else {
                        "An online landing page alerts you that your smartphone has critical system leaks and prompts you to download and install an enterprise 'Configuration Profile'. This closely replicates advanced zero-click Pegasus spyware vector deployments. What is your action?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) تثبيت الملف لضمان استقرار هاتفك الأمني فورا وصيانة المشكلة.",
                            "ب) رفض تثبيت أي ملفات مواصفات تعريف غير موثقة، وفحص الشهادات النشطة حالياً عبر المسار الأمني.",
                            "ج) إعادة تشغيل الهاتف وتثبيت الملف في الوضع الآمن للتأكد من سلامته.",
                            "د) التقاط لقطة شاشة للاستفسار في المجموعات الأمنية قبل البدء."
                        )
                    } else {
                        listOf(
                            "A) Proceed with the installation to maintain system upgrades.",
                            "B) Reject the configuration install prompt, verify active MDM certificates globally, and enable strict Lockdown/Isolated sandbox protocols.",
                            "C) Restart the kernel and install in safe mode to parse telemetry safely.",
                            "D) Take multiple snapshots to analyze later on a public sandbox forum."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "ملفات التعريف قد تحتوي على برمجيات تجسس كاملة، لا تثبتها أبدا من مصادر غير موثوقة." else "Profiles can host full spyware; never install from untrusted web sources."
                ),
                AcademyScenario(
                    id = "off_ms_2",
                    scenario = if (isAr) {
                        "لاحظت أن تطبيقاً للألعاب قمت بتثبيته يطلب أذونات الوصول إلى جهات الاتصال والميكروفون والكاميرا والموقع. هذه الأذونات غير ضرورية للعبة. ما الذي يجب فعله؟"
                    } else {
                        "A newly installed gaming app requests permissions for contacts, microphone, camera, and location — all unnecessary for gaming. This is a common mobile over-permissioning attack vector. What should you do?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) قبول جميع الأذونات لأن اللعبة لن تعمل بدونها.",
                            "ب) قبول أذونات الميكروفون فقط وتجاهل الباقي.",
                            "ج) رفض جميع الأذونات غير الضرورية، أو حذف التطبيق والبحث عن بديل موثوق من متجر رسمي.",
                            "د) منح الأذونات وإيقاف تشغيل الإنترنت أثناء اللعب."
                        )
                    } else {
                        listOf(
                            "A) Accept all permissions since the game might not work otherwise.",
                            "B) Accept only microphone access and ignore the rest.",
                            "C) Deny all unnecessary permissions or uninstall and find a verified alternative from official stores.",
                            "D) Grant permissions but disconnect from internet while playing."
                        )
                    },
                    correctIndex = 2,
                    explanation = if (isAr) "كل إذن غير ضروري هو ثغرة محتملة. رفض الأذونات الزائدة أو حذف التطبيق هو الخيار الأمثل." else "Every unnecessary permission is a potential vulnerability. Deny excess permissions or remove the app."
                ),
                AcademyScenario(
                    id = "off_ms_3",
                    scenario = if (isAr) {
                        "أثناء استخدامك لمنفذ USB عام في المطار لشحن هاتفك، لاحظت ظهور نافذة منبثقة تطلب منك السماح بالوصول للبيانات. ما الإجراء الصحيح؟"
                    } else {
                        "While charging your phone at a public USB port in the airport (Juice Jacking risk), a popup appears asking if you trust this device and want to allow data access. What is the correct action?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) قبول الوصول للبيانات لأن المطار مكان آمن.",
                            "ب) رفض أي وصول للبيانات واستخدام شاحن جداري خاص أو بطارية احتياطية فقط.",
                            "ج) قبول الوصول للبيانات وفصل الهاتف بعد الشحن مباشرة.",
                            "د) تشغيل وضع الطيران أثناء الشحن."
                        )
                    } else {
                        listOf(
                            "A) Accept data access since it's a public airport.",
                            "B) Reject all data access and use only a personal wall charger or power bank.",
                            "C) Accept data access but unplug immediately after charging completes.",
                            "D) Enable airplane mode while charging via public USB."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "منافذ USB العامة يمكن استخدامها لسرقة البيانات. رفض الوصول واستخدام شاحنك الخاص هو الحل الوحيد الآمن." else "Public USB ports can be used for data theft (Juice Jacking). Always refuse data access and use your own charger."
                )
            )
            "network_safety" -> listOf(
                AcademyScenario(
                    id = "off_ns_1",
                    scenario = if (isAr) {
                        "أثناء اتصالك بشبكة Wi-Fi غير آمنة في مطار، تدرك وجود هجمات اعتراض نشطة (MITM) تحاول إرسال تحديثات شهادات Root خبيثة للاستماع لكامل اتصالاتك الحيوية."
                    } else {
                        "While connected to an open airport lounge Wi-Fi network, your terminal issues SSL verification warnings and attempts to inject a hostile root CA certificate, mimicking a tactical Man-in-the-Middle (MITM) attack. How do you respond?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) تفعيل شهادة الجذر لتخطي قيود الشبكة والاتصال السريع.",
                            "ب) إزالة الاتصال بالشبكة كلياً، والانتقال إلى ناقل البيانات الخليوي واستعمال نفق VPN عسكري تكتيكي متماسك.",
                            "ج) الاستمرار بالتصفح مع تجاهل تحذيرات شهادات الأمان (HTTPS Warnings).",
                            "د) مسح ذاكرة التخزين المؤقت للمتصفح ومحاولة الضغط المستمر."
                        )
                    } else {
                        listOf(
                            "A) Accept the updated root CA to bypass the airport captive portal limits.",
                            "B) Sever the Wi-Fi connection immediately, fallback to secure cellular LTE data, and establish a military-grade private VPN tunnel.",
                            "C) Continue browsing using alternative browsers while suppressing security warnings.",
                            "D) Clear regional cookies and flush DNS tables continuously."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "شهادات الجذر تمنح المهاجم قدرة كاملة على فك تشفير اتصالاتك، اقطع الاتصال فوراً." else "Root CAs allow full decryption of your traffic; sever the link immediately."
                ),
                AcademyScenario(
                    id = "off_ns_2",
                    scenario = if (isAr) {
                        "اكتشفت أن شبكة Wi-Fi جارك تظهر كشبكة مفتوحة بدون كلمة مرور. قررت الاتصال بها لأن الاتصال الخاص لديك بطيء. هل هذا القرار صحيح؟"
                    } else {
                        "You discover your neighbor's Wi-Fi network appears as an open network without a password. You consider connecting because your own connection is slow. Is this the right decision?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) نعم، الاتصال بالشبكة المجانية المفتوحة يوفر المال.",
                            "ب) لا، الشبكات المفتوحة تعرّض بياناتك لخطر الاعتراض من صاحب الشبكة أو الآخرين على نفس الشبكة.",
                            "ج) الاتصال مؤقتاً فقط لمهام بسيطة بدون بيانات حساسة.",
                            "د) الاتصال لكن مع تفعيل VPN."
                        )
                    } else {
                        listOf(
                            "A) Yes, connecting to the free open network saves money.",
                            "B) No — open networks expose your data to interception by the network owner or other users on the same network.",
                            "C) Connect temporarily only for simple tasks without sensitive data.",
                            "D) Connect but activate a VPN to stay safe."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "الشبكات المفتوحة تفتقر للتشفير، مما يعرّض بياناتك للاعتراض. لا تتصل بها أبداً دون VPN موثوق." else "Open networks lack encryption, exposing your traffic. Never connect without a trusted VPN — and ideally avoid them entirely."
                ),
                AcademyScenario(
                    id = "off_ns_3",
                    scenario = if (isAr) {
                        "وصلك إشعار من جهاز التوجيه المنزلي (Router) يطلب تحديث البرنامج الثابت عبر رابط في الإشعار نفسه. هل تقوم بتثبيت التحديث؟"
                    } else {
                        "You receive a notification from your home router asking you to update its firmware via a link embedded in the notification itself. Should you install this update?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) نعم، التحديثات دائماً ضرورية ويجب تثبيتها.",
                            "ب) لا، قم بزيارة موقع الشركة المصنّعة مباشرة للتحقق من التحديث وتثبيته من المصدر الرسمي.",
                            "ج) تثبيت التحديث وإعادة تشغيل الجهاز.",
                            "د) تجاهل الإشعار لأن التحديثات تسبب مشاكل عادةً."
                        )
                    } else {
                        listOf(
                            "A) Yes, updates are always necessary and should be installed.",
                            "B) No — visit the manufacturer's website directly to verify and install the update from the official source.",
                            "C) Install the update and restart the device.",
                            "D) Ignore the notification since updates often cause problems."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "روابط التحديث في الإشعارات قد تكون مزيفة. دائماً تحقق من التحديثات عبر الموقع الرسمي للشركة." else "Update links in notifications can be fake. Always verify updates from the manufacturer's official website."
                )
            )
            else -> listOf(
                AcademyScenario(
                    id = "off_pr_1",
                    scenario = if (isAr) {
                        "أكتشفت قيام أداة ألعاب بمسح بياناتك التعريفية وسجل المواقع والصفحات السابقة عبر تقنيات سحب البيانات التعويضية وملفات التعريف بدون موافقتك الصريحة."
                    } else {
                        "A modern web tool is discovered to be programmatically scraping your historical metadata, search trends, and localized search buffers using cross-site third-party tracker APIs without consent. How do you handle this?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) حذف الملف المؤقت للمتصفح دون تعديل أذونات الخصوصية العامة.",
                            "ب) حظر ملفات تتبع الطرف الثالث عبر المتصفح (3rd-Party Cookies)، وتفعيل طلبات عدم تتبع البيانات، واستخدام محرك بحث يحمي الخصوصية.",
                            "ج) شكوى الخدمة الموفرة لرفع دعوى جماعية كخطوة وحيدة.",
                            "د) كتم الإعلانات الفردية يدوياً للتخلص من الملاحقات."
                        )
                    } else {
                        listOf(
                            "A) Delete the temporary browser folder and clear local system directories manually.",
                            "B) Restrict all third-party cookies, enforce strict anti-fingerprint blocks in browser profiles, and shift searches to zero-logging networks.",
                            "C) File a privacy appeal under global frameworks as your primary reactive response.",
                            "D) Suppress individual personalized ad tracking units manually each session."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "استخدام أدوات حماية الخصوصية بشكل استباقي أفضل من محاولة إصلاح الضرر لاحقا." else "Proactive privacy tools are superior to reactive cleanup methods."
                ),
                AcademyScenario(
                    id = "off_pr_2",
                    scenario = if (isAr) {
                        "عند التسجيل في خدمة جديدة، طُلب منك الموافقة على سياسة الخصوصية. قررت الضغط على 'أوافق' مباشرة دون قراءتها. ما هو الخطر في ذلك؟"
                    } else {
                        "When signing up for a new service, you are asked to agree to a privacy policy. You press 'I Agree' without reading it. What is the primary risk associated with this behavior?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) لا يوجد خطر، فهذه مجرد وثيقة قانونية روتينية.",
                            "ب) ربما تكون وافقت على مشاركة بياناتك مع أطراف ثالثة أو استخدامها للإعلانات المستهدفة.",
                            "ج) قد يُحظر حسابك لاحقاً بسبب انتهاك الشروط.",
                            "د) لا يمكن للشركة مشاركة بياناتك دون إذن صريح في كل مرة."
                        )
                    } else {
                        listOf(
                            "A) No risk — it's just a routine legal document.",
                            "B) You may have consented to sharing your data with third parties or use for targeted advertising.",
                            "C) Your account could later be banned for violating terms.",
                            "D) Companies cannot share your data without explicit permission each time."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "سياسات الخصوصية غالباً تتضمن بنوداً تسمح لهم بمشاركة أو بيع بياناتك. اقرأها دائماً أو استخدم مواقع تلخيصها." else "Privacy policies often include clauses allowing data sharing or sale. Always read them or use services that summarize them."
                ),
                AcademyScenario(
                    id = "off_pr_3",
                    scenario = if (isAr) {
                        "تستخدم تطبيقاً للمحادثة يدّعي أن رسائلك مشفرة. أرسل صديقك رسالة حساسة. ما الذي يجب التحقق منه للتأكد من الأمان الفعلي؟"
                    } else {
                        "You use a messaging app that claims your messages are encrypted. A friend sends you sensitive information. What should you verify to ensure actual security?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) الادعاء بالتشفير كافٍ، لا حاجة للتحقق.",
                            "ب) التحقق من أن التشفير من طرف إلى طرف (End-to-End) مفعّل، والتطبيق مفتوح المصدر أو مدقق أمنياً بشكل مستقل.",
                            "ج) استخدام شبكة VPN يكفي لحماية المحادثات.",
                            "د) التحقق من عدد المستخدمين كمؤشر على الأمان."
                        )
                    } else {
                        listOf(
                            "A) Claiming encryption is sufficient — no need to verify.",
                            "B) Verify that End-to-End Encryption (E2EE) is active, and that the app is open-source or independently audited.",
                            "C) Using a VPN is enough to protect conversations.",
                            "D) Check the number of users as a security indicator."
                        )
                    },
                    correctIndex = 1,
                    explanation = if (isAr) "الادعاء بالتشفير لا يكفي. التشفير من طرف إلى طرف المدقق مستقلاً هو المعيار الذهبي للخصوصية." else "Claiming encryption isn't enough. Verified, independently audited E2EE is the gold standard for private communications."
                )
            )
        }
    }

    var cooldownTimer by remember { mutableIntStateOf(0) }
    LaunchedEffect(cooldownTimer) {
        if (cooldownTimer > 0) {
            delay(1000)
            cooldownTimer -= 1
        }
    }

    fun triggerQuestionGeneration() {
        if (cooldownTimer > 0) return
        cooldownTimer = 5 // Phase 25: 5s Cooldown Shield
        isGeneratingScenarios = true
        scenariosList = emptyList() // Ensure old questions are cleared
        currentScenarioIndex = 0
        selectedOptionIndex = null
        debriefText = ""
        hasAnsweredCurrent = false

        // Build list of past scenario text snippets (first 120 chars) — the AI uses these to avoid repeating
        val usedTopicSnippets = usedScenarioIds.toList().mapNotNull { id ->
            // Try to find the scenario in the current list, or use a persistent store if available
            scenariosList.find { it.id == id }?.scenario?.take(120)
        }

        viewModel.generateAcademyScenarios(
            moduleNameEn = module.titleEn,
            moduleNameAr = module.titleAr,
            useArabic = isAr,
            usedTopics = usedTopicSnippets,
            onSuccess = { jsonString ->
                try {
                    val root = JSONObject(jsonString)
                    val array = root.getJSONArray("scenarios")
                    val parsed = mutableListOf<AcademyScenario>()
                    for (i in 0 until array.length()) {
                        val itemObj = array.getJSONObject(i)
                        val id = itemObj.optString("id", "gen_${System.currentTimeMillis()}_$i")
                        val scenarioText = itemObj.getString("scenario")
                        val optsArr = itemObj.getJSONArray("options")
                        val options = mutableListOf<String>()
                        for (j in 0 until optsArr.length()) {
                            options.add(optsArr.getString(j))
                        }
                        val correct = itemObj.getInt("correctIndex")
                        val explanation = itemObj.optString("explanation", "")
                        parsed.add(AcademyScenario(id, scenarioText, options, correct, explanation))
                    }
                    if (parsed.isNotEmpty()) {
                        // IMPORTANT: We only use generated scenarios if successful to avoid repetition
                        parsed.forEach { usedScenarioIds.add(it.id) }
                        scenariosList = parsed.shuffled()
                    } else {
                        // Fallback only if JSON was empty
                        scenariosList = offlineScenarios.shuffled().take(3)
                    }
                } catch (e: Exception) {
                    // Log error but don't just silently show old questions if we can avoid it
                    debriefText = "ERROR_PARSING_NEURAL_DATA: ${e.message}"
                    scenariosList = offlineScenarios.shuffled().take(3)
                } finally {
                    isGeneratingScenarios = false
                }
            },
            onFailure = { error ->
                // Force a notification to the user that we are in offline mode
                val errorMsg = error.message ?: "Unknown Connection Error"
                println("Academy Generation Failed: $errorMsg")
                
                // Still show some questions so the app doesn't break, but clear the used list to refresh
                if (usedScenarioIds.size > 10) usedScenarioIds.clear() 
                scenariosList = offlineScenarios.shuffled().take(3)
                
                isGeneratingScenarios = false
                if (errorMsg.contains("503") || errorMsg.contains("BUSY") || errorMsg.contains("429")) {
                    cooldownTimer = 15
                }
            }
        )
    }

    // Trigger dynamic scenarios loading only if no saved state for this module
    LaunchedEffect(module.id) {
        if (!isSameModule || scenariosList.isEmpty()) {
            triggerQuestionGeneration()
        }
    }

    val isAcademyGenerating by viewModel.isAcademyGenerating.collectAsState()
      val academyPoints by viewModel.academyPoints.collectAsState()
      val academyStreak by viewModel.academyStreak.collectAsState()

    if (isAcademyGenerating) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isAr) "استدعاء السيناريوهات السيادية للجلسة..." else "INVOKING SOVEREIGN SESSION SCENARIOS...",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerEffect(modifier = Modifier.height(60.dp))
                ShimmerEffect(modifier = Modifier.height(100.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isAr) "تحسين نماذج الهجوم والدفاع العصبية" else "OPTIMIZING NEURAL OFFENSE/DEFENSE MODELS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAr) "🡠 عودة للمناهج" else "🡠 BACK TO SYLLABUS",
                color = AmberZen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onBack() 
                    }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

          // Stats bar — XP + Streak
          androidx.compose.foundation.layout.Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 12.dp)
                  .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                  .background(CyberCyan.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                  .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
          ) {
              Text(
                  text = "⚡ $academyPoints XP",
                  color = CyberCyan,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
              )
              if (academyStreak > 0) {
                  Text(
                      text = if (isAr) "🔥 $academyStreak يوم" else "🔥 $academyStreak day streak",
                      color = Color(0xFFFFAA00),
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                  )
              } else {
                  Text(
                      text = if (isAr) "ابدأ سلسلتك اليوم!" else "Start your streak!",
                      color = Color.White.copy(alpha = 0.3f),
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                  )
              }
          }

          if (isGeneratingScenarios) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isAr) "جاري استدعاء العقل الاصطناعي..." else "DYNAMIC COMPILING VIA GEMINI COGNITION...",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isAr) "تحليل حوادث الاختراقات الواقعية وبناء السيناريو الفعلي..." else "Synthesizing educational simulations targeting historical breaches...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (scenariosList.isNotEmpty()) {
            val currentScenario = scenariosList[currentScenarioIndex]

            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) {
                        "السؤال ${currentScenarioIndex + 1} من ${scenariosList.size}"
                    } else {
                        "QUESTION ${currentScenarioIndex + 1} OF ${scenariosList.size}"
                    },
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isAr) "مستوى النقاط: +25 نقطة" else "REWARD: +25 PTS",
                    color = AmberZen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scenario Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentScenario.scenario,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isAr) "اختر الحل التكتيكي الأكثر أماناً وحماية:" else "CHOOSE THE SECURE STRATEGIC TACTIC:",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Render options beautifully
            currentScenario.options.forEachIndexed { idx, option ->
                val isSelected = selectedOptionIndex == idx
                val isCorrect = idx == currentScenario.correctIndex
                
                val optionBorderColor = when {
                    isSelected -> if (hasAnsweredCurrent) {
                        if (isCorrect) Color.Green else Color.Red
                    } else CyberCyan
                    hasAnsweredCurrent && isCorrect -> Color.Green.copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.15f)
                }
                
                val optionBgColor = when {
                    isSelected -> if (hasAnsweredCurrent) {
                        if (isCorrect) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                    } else CyberCyan.copy(alpha = 0.08f)
                    hasAnsweredCurrent && isCorrect -> Color.Green.copy(alpha = 0.05f)
                    else -> Color.White.copy(alpha = 0.02f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, optionBorderColor, RoundedCornerShape(10.dp))
                        .background(optionBgColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !hasAnsweredCurrent) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            selectedOptionIndex = idx
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) CyberCyan else Color.Transparent,
                            border = BorderStroke(1.1.dp, if (isSelected) CyberCyan else Color.White.copy(alpha = 0.4f))
                        ) {}
                        Text(
                            text = option,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            if (!hasAnsweredCurrent) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val finalChoice = selectedOptionIndex ?: return@Button
                        hasAnsweredCurrent = true
                        
                        // Score Addition
                          if (finalChoice == currentScenario.correctIndex) {
                              viewModel.addCyberScore(10)
                              viewModel.recordCorrectAnswer()
                          }

                        // Generate Adaptive Feedback via Gemini
                        isGeneratingDebrief = true
                        debriefText = ""
                        viewModel.generateStrategicDebrief(
                            scenario = currentScenario.scenario,
                            choiceText = currentScenario.options[finalChoice],
                            useArabic = isAr,
                            onResponse = { responseDebrief ->
                                if (responseDebrief.isNotBlank()) {
                                    debriefText = responseDebrief
                                    if (currentScenario.explanation.isNotBlank()) {
                                        debriefText += "\n\n[ ANALYSIS ]: ${currentScenario.explanation}"
                                    }
                                } else {
                                    // Local specific offline feedback
                                    debriefText = if (isAr) {
                                        if (finalChoice == currentScenario.correctIndex) "قرار ممتاز ومطابق للأمن السيبراني العالي! الوعي الصارم والتحقق يفشلان عمليات الاختراق. تم منح +10 نقاط." else "تصرف عالي الخطورة. تذكر دائماً عزل قنوات الاتصال والامتناع عن منح رموز التحقق أو الملفات المشبوهة."
                                    } else {
                                        if (finalChoice == currentScenario.correctIndex) "Action secure. Zero-Trust validation stops real-world malicious breach vectors perfectly! +10 PTS awarded." else "Critical operational vulnerability identified. Remember to always sever unverified configurations or channels immediately."
                                    }
                                    if (currentScenario.explanation.isNotBlank()) {
                                        debriefText += "\n\n[ ANALYSIS ]: ${currentScenario.explanation}"
                                    }
                                }
                                isGeneratingDebrief = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.1f),
                        disabledContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    border = BorderStroke(1.dp, if (selectedOptionIndex != null) CyberCyan else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = selectedOptionIndex != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isAr) "إرسال الجواب للتحقق" else "SUBMIT DEPLOYED ANSWER",
                        color = if (selectedOptionIndex != null) CyberCyan else Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Adaptive Feedback Debrief
            AnimatedVisibility(
                visible = hasAnsweredCurrent && (isGeneratingDebrief || debriefText.isNotEmpty()),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberZen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .background(GlassWhite, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmberZen, RoundedCornerShape(50))
                        )
                        Text(
                            text = if (isAr) "التقرير التكتيكي الاستراتيجي DEBRIEF:" else "STRATEGIC OPERATIONS DEBRIEF:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isGeneratingDebrief) {
                        Text(
                            text = if (isAr) "جاري استدعاء المحللين التكتيكيين..." else "Connecting to high-order threat matrices for decision analytics...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = debriefText,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

        // Next Question or Exit
        if (hasAnsweredCurrent && !isGeneratingDebrief) {
            Spacer(modifier = Modifier.height(18.dp))
            
            // Show "Generate New Questions" ONLY after answering ALL 3 questions
            val isLastQuestion = currentScenarioIndex + 1 >= scenariosList.size
            if (isLastQuestion) {
                Button(
                    onClick = { triggerQuestionGeneration() },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberZen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, if (cooldownTimer == 0) AmberZen else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = cooldownTimer == 0 && !isGeneratingScenarios,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (cooldownTimer > 0) {
                            if (isAr) "جارٍ تحميل أسئلة جديدة ($cooldownTimer)..." else "LOADING NEW QUESTIONS ($cooldownTimer)..."
                        } else {
                            if (isAr) "✦ توليد مجموعة أسئلة جديدة" else "✦ GENERATE NEW QUESTION SET"
                        },
                        color = if (cooldownTimer == 0) AmberZen else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    if (currentScenarioIndex + 1 < scenariosList.size) {
                        currentScenarioIndex++
                        selectedOptionIndex = null
                        debriefText = ""
                        hasAnsweredCurrent = false
                    } else {
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (isAr) {
                        if (currentScenarioIndex + 1 < scenariosList.size) "السيناريو التالي 🡠" else "عودة للمناهج"
                    } else {
                        if (currentScenarioIndex + 1 < scenariosList.size) "NEXT SCENARIO 🡠" else "COMPLETE MODULE"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        }
    }
}
}
