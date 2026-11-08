package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import android.net.Uri
import com.example.data.database.VaultFileRepository
import com.example.data.database.VaultItem
import com.example.data.database.InventorIdea
import com.example.data.database.InventorIdeaRepository
import com.example.data.ContextualVerseEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import com.example.adaptive.AdaptiveEngine
  import com.example.adaptive.FeedbackResult
  import com.example.adaptive.FeedbackService
  import com.example.adaptive.UIChange
  import com.example.data.SovereignDataStore
import com.example.data.remote.GeminiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DashboardViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var connectivityCallback: ConnectivityManager.NetworkCallback? = null
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val alpha = 0.12f
    private var currentRoll = 0f
    private var currentPitch = 0f
    
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private val _isAcademyGenerating = MutableStateFlow(false)
    val isAcademyGenerating: StateFlow<Boolean> = _isAcademyGenerating.asStateFlow()

    fun startSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    private val _connectionType = MutableStateFlow("Determining...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(100)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _chargingStatus = MutableStateFlow("UNKNOWN")
    val chargingStatus: StateFlow<String> = _chargingStatus.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _intelligenceBrief = MutableStateFlow("[INTEL_UPLINK] > Initializing satellite intelligence stream...")
    val intelligenceBrief: StateFlow<String> = _intelligenceBrief.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    private val _terminalResponse = MutableStateFlow("")
    val terminalResponse: StateFlow<String> = _terminalResponse.asStateFlow()

    private val _isTerminalExpanded = MutableStateFlow(false)
    val isTerminalExpanded: StateFlow<Boolean> = _isTerminalExpanded.asStateFlow()

    private val _isNeuralLinkOffline = MutableStateFlow(false)
    val isNeuralLinkOffline: StateFlow<Boolean> = _isNeuralLinkOffline.asStateFlow()

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection.asStateFlow()

    private val _isTestingKey = MutableStateFlow(false)
    val isTestingKey: StateFlow<Boolean> = _isTestingKey.asStateFlow()

    private val _linkAnalysisResult = MutableStateFlow<String?>(null)
    val linkAnalysisResult: StateFlow<String?> = _linkAnalysisResult.asStateFlow()

    private val _isAnalyzingLink = MutableStateFlow(false)
    val isAnalyzingLink: StateFlow<Boolean> = _isAnalyzingLink.asStateFlow()

    private val database = AppDatabase.getDatabase(application)
    private val repository = InventorIdeaRepository(database.inventorIdeaDao())
    private val vaultFileRepository = VaultFileRepository(application, database.vaultItemDao())

    val vaultItems: StateFlow<List<VaultItem>> = vaultFileRepository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun importVaultFile(uri: Uri): VaultItem = vaultFileRepository.importFromUri(uri)

    suspend fun saveVaultText(title: String, text: String): VaultItem =
        vaultFileRepository.saveText(title, text)

    suspend fun exportVaultFile(id: String, destination: Uri) =
        vaultFileRepository.exportToUri(id, destination)

    suspend fun deleteVaultItem(id: String) {
        check(_isVaultAuthenticated.value) { "Vault authentication is required." }
        vaultFileRepository.delete(id)
    }

    val savedIdeas: StateFlow<List<InventorIdea>> = repository.allIdeas
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val ambientInsight: StateFlow<ContextualVerseEngine.SpiritualInsight> = combine(
        _batteryPercentage, _chargingStatus, _connectionType
    ) { battery, charging, connection ->
        ContextualVerseEngine.getInsight(battery, charging == "CHARGING", connection)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContextualVerseEngine.getInsight(100, false, "Determining..."))

    private val _isVaultViewOpen = MutableStateFlow(false)
    val isVaultViewOpen: StateFlow<Boolean> = _isVaultViewOpen.asStateFlow()

    private val _isVaultAuthenticated = MutableStateFlow(false)
    val isVaultAuthenticated: StateFlow<Boolean> = _isVaultAuthenticated.asStateFlow()

    private val _isSettingsAuthenticated = MutableStateFlow(false)
    val isSettingsAuthenticated: StateFlow<Boolean> = _isSettingsAuthenticated.asStateFlow()

    fun setVaultAuthenticated(auth: Boolean) { _isVaultAuthenticated.value = auth }
    fun setSettingsAuthenticated(auth: Boolean) { _isSettingsAuthenticated.value = auth }

    private val dataStore = SovereignDataStore(application)
      val adaptiveEngine   = AdaptiveEngine(application)
      val uiConfig         = adaptiveEngine.uiConfig
      private val feedbackService = FeedbackService(application)
      private val geminiRepository = GeminiRepository()

    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    fun setArabic(enabled: Boolean) {
        viewModelScope.launch { dataStore.saveIsArabic(enabled) }
    }

    private val _cyberScore = MutableStateFlow(0)
    val cyberScore: StateFlow<Int> = _cyberScore.asStateFlow()

    private val _operatorName = MutableStateFlow("Sovereign_Operator")
    val operatorName: StateFlow<String> = _operatorName.asStateFlow()

    private val _neuralRole = MutableStateFlow("Sovereign Node v4")
    val neuralRole: StateFlow<String> = _neuralRole.asStateFlow()

    val cyberRank: StateFlow<String> = combine(_cyberScore, _isArabic) { score, isAr ->
        if (isAr) {
            when {
                score <= 50 -> "شيفرة مبتدئة"
                score <= 150 -> "حارس السينتينل"
                else -> "شبح سيادي"
            }
        } else {
            when {
                score <= 50 -> "Novice Cipher"
                score <= 150 -> "Sentinel Guard"
                else -> "Sovereign Ghost"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Novice Cipher")

    val cyberProgress: StateFlow<Float> = combine(_cyberScore, MutableStateFlow(Unit)) { score, _ ->
        when {
            score <= 50 -> (score.toFloat() / 50f).coerceIn(0f, 1f)
            score <= 150 -> ((score - 50).toFloat() / 100f).coerceIn(0f, 1f)
            else -> 1.0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _isAcademyOpen = MutableStateFlow(false)
    val isAcademyOpen: StateFlow<Boolean> = _isAcademyOpen.asStateFlow()

    private val _isResourcesOpen = MutableStateFlow(false)
    val isResourcesOpen: StateFlow<Boolean> = _isResourcesOpen.asStateFlow()

    fun addCyberScore(points: Int) {
        viewModelScope.launch(Dispatchers.IO) { dataStore.addCyberScore(points) }
    }


    fun saveUserProfile(gender: String, birthYear: Int) {
        viewModelScope.launch {
            dataStore.saveUserGender(gender)
            dataStore.saveUserBirthYear(birthYear)
            dataStore.setProfileCompleted()
        }
    }

      fun completeOnboarding() {
          viewModelScope.launch { dataStore.setOnboardingCompleted() }
      }

      fun recordCorrectAnswer() {
          viewModelScope.launch {
              dataStore.addAcademyPoints(10)
              dataStore.updateAcademyStreak()
          }
      }

          /** Submit UI-customization feedback using the app's single Gemini API key. */
      suspend fun submitFeedback(text: String): FeedbackResult {
          return feedbackService.submitFeedback(text, _customApiKey.value)
      }

      /** Apply a confirmed list of UIChange items via AdaptiveEngine. */
      fun applyUIChanges(changes: List<UIChange>) {
          adaptiveEngine.applyChanges(changes)
      }

      /** Reset all adaptive UI settings to factory defaults. */
      fun resetUIToDefaults() {
          adaptiveEngine.resetToDefaults()
      }

    fun setAcademyOpen(open: Boolean) { _isAcademyOpen.value = open }

    private val _savedAcademyModuleId = MutableStateFlow("")
    private val _savedAcademyScenariosJson = MutableStateFlow("")
    private val _savedAcademyIndex = MutableStateFlow(0)
    private val _savedAcademyUsedIds = MutableStateFlow<Set<String>>(emptySet())

    fun saveAcademyProgress(moduleId: String, scenariosJson: String, index: Int, usedIds: Set<String>) {
        _savedAcademyModuleId.value = moduleId
        _savedAcademyScenariosJson.value = scenariosJson
        _savedAcademyIndex.value = index
        _savedAcademyUsedIds.value = usedIds
    }

    fun getSavedAcademyModuleId(): String = _savedAcademyModuleId.value
    fun getSavedAcademyScenariosJson(): String = _savedAcademyScenariosJson.value
    fun getSavedAcademyIndex(): Int = _savedAcademyIndex.value
    fun getSavedAcademyUsedIds(): Set<String> = _savedAcademyUsedIds.value

    fun setResourcesOpen(open: Boolean) { _isResourcesOpen.value = open }

    private val _isStealthMode = MutableStateFlow(false)
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _projectId = MutableStateFlow("")
    val projectId: StateFlow<String> = _projectId.asStateFlow()

    private val _projectNumber = MutableStateFlow("")
    val projectNumber: StateFlow<String> = _projectNumber.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // --- User Cognitive Model State ---
    private val _userLevel = MutableStateFlow("Beginner")
    val userLevel: StateFlow<String> = _userLevel.asStateFlow()

    private val _userInterests = MutableStateFlow<Set<String>>(emptySet())
    val userInterests: StateFlow<Set<String>> = _userInterests.asStateFlow()
    private val _userGoal = MutableStateFlow("")
    val userGoal: StateFlow<String> = _userGoal.asStateFlow()

    private val _calibrationCompleted = MutableStateFlow(false)
      val calibrationCompleted: StateFlow<Boolean> = _calibrationCompleted.asStateFlow()

      private val _onboardingCompleted = MutableStateFlow(false)
      val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

      private val _academyPoints = MutableStateFlow(0)
      val academyPoints: StateFlow<Int> = _academyPoints.asStateFlow()

      private val _academyStreak = MutableStateFlow(0)
      val academyStreak: StateFlow<Int> = _academyStreak.asStateFlow()

    // --- DataStore loaded indicator ---
    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    // --- User Profile ---
    private val _userGender = MutableStateFlow("")
    val userGender: StateFlow<String> = _userGender.asStateFlow()
    private val _userBirthYear = MutableStateFlow(0)
    val userBirthYear: StateFlow<Int> = _userBirthYear.asStateFlow()
    private val _userProfileAnalysis = MutableStateFlow("")
    val userProfileAnalysis: StateFlow<String> = _userProfileAnalysis.asStateFlow()
    // --- End User Cognitive Model State ---

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS).callTimeout(12, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true).build()

    private suspend fun generateGeminiText(prompt: String): String {
        val apiKey = _customApiKey.value
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API key is missing. Add it in Settings.")
        }
        return geminiRepository.generateText(
            systemInstruction = "You are the AI assistant inside A.SYRIA. Follow the task in the user input and respond in the requested language.",
            input = prompt,
            apiKey = apiKey
        )
    }

    fun setStealthMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.saveStealthMode(enabled).also { _isStealthMode.value = enabled } }
    }

    fun updateCustomApiKey(key: String) {
        viewModelScope.launch { dataStore.saveGeminiApiKey(key) }
    }

    fun updateProjectName(name: String) {
        viewModelScope.launch { dataStore.saveProjectName(name) }
    }

    fun updateProjectId(id: String) {
        viewModelScope.launch { dataStore.saveProjectId(id) }
    }

    fun updateProjectNumber(number: String) {
        viewModelScope.launch { dataStore.saveProjectNumber(number) }
    }

    fun updateOperatorName(name: String) {
        viewModelScope.launch { dataStore.saveOperatorName(name) }
    }

    fun updateNeuralRole(role: String) {
        viewModelScope.launch { dataStore.saveNeuralRole(role) }
    }

    fun setSettingsOpen(open: Boolean) { _isSettingsOpen.value = open }

    // --- Function to save calibration data ---
    fun saveCalibrationData(level: String, interests: Set<String>, goal: String = "") {
        viewModelScope.launch {
            dataStore.saveUserLevel(level)
            dataStore.saveUserInterests(interests)
            if (goal.isNotBlank()) dataStore.saveUserGoal(goal)
            dataStore.saveCalibrationCompleted(true)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) { _batteryPercentage.value = ((level.toFloat() / scale.toFloat()) * 100).toInt() }
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                _chargingStatus.value = if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) "CHARGING" else "DISCHARGING"
            }
        }
    }

    fun testNeuralLink() {
        viewModelScope.launch {
            _isTestingKey.value = true
            val apiKey = _customApiKey.value
            if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY")) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] Gemini API Key is required. Add your Gemini API key in Settings."
                _isTestingKey.value = false
                return@launch
            }
            try {
                kotlinx.coroutines.withTimeout(15000L) {
                    generateGeminiText("ping")
                }
                _isNeuralLinkOffline.value = false
                _terminalResponse.value = "FULL NEURAL LINK ESTABLISHED ✅"
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] ${e.message}"
            } finally {
                _isTestingKey.value = false
            }
        }
    }

    init {
        viewModelScope.launch { dataStore.clearLegacyAiSettings() }
        viewModelScope.launch(Dispatchers.IO) { dataStore.migrateLegacyGeminiApiKey() }
        application.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        monitorConnectivity()
        viewModelScope.launch { dataStore.isArabic.collect { _isArabic.value = it } }
        viewModelScope.launch { dataStore.stealthMode.collect { _isStealthMode.value = it } }
        viewModelScope.launch { dataStore.cyberScore.collect { _cyberScore.value = it } }
        viewModelScope.launch { dataStore.geminiApiKey.collect { _customApiKey.value = it } }
        viewModelScope.launch { dataStore.projectName.collect { _projectName.value = it } }
        viewModelScope.launch { dataStore.projectId.collect { _projectId.value = it } }
        viewModelScope.launch { dataStore.projectNumber.collect { _projectNumber.value = it } }
        viewModelScope.launch { dataStore.operatorName.collect { _operatorName.value = it } }
        viewModelScope.launch { dataStore.neuralRole.collect { _neuralRole.value = it } }
        // Collect user profile data
        viewModelScope.launch { dataStore.userLevel.collect { _userLevel.value = it } }
        viewModelScope.launch { dataStore.userInterests.collect { _userInterests.value = it } }
        viewModelScope.launch { dataStore.calibrationCompleted.collect { _calibrationCompleted.value = it } }
        viewModelScope.launch { dataStore.userGoal.collect { _userGoal.value = it } }
          viewModelScope.launch { dataStore.onboardingCompleted.collect { _onboardingCompleted.value = it } }
          viewModelScope.launch { dataStore.academyPoints.collect { _academyPoints.value = it } }
          viewModelScope.launch { dataStore.academyStreak.collect { _academyStreak.value = it } }


        viewModelScope.launch { dataStore.userGender.collect { _userGender.value = it } }
        viewModelScope.launch { dataStore.userBirthYear.collect { _userBirthYear.value = it } }
        viewModelScope.launch { dataStore.userProfileAnalysis.collect { _userProfileAnalysis.value = it } }
        // Mark data as loaded after critical fields are available
        viewModelScope.launch {
            dataStore.onboardingCompleted.first()
            _isDataLoaded.value = true
        }
        viewModelScope.launch(Dispatchers.IO) {
            try { _ipAddress.value = fetchPublicIp() } catch (_: Exception) { _ipAddress.value = extractLocalIp() }
        }
    }

    fun setTerminalExpanded(expanded: Boolean) {
        _isTerminalExpanded.value = expanded
        if (!expanded) { _terminalInput.value = ""; _terminalResponse.value = "" }
    }

    fun deleteIdea(ideaId: Int) {
        if (!_isVaultAuthenticated.value) return
        viewModelScope.launch(Dispatchers.IO) { repository.deleteIdeaById(ideaId) }
    }

    suspend fun wipeVault() {
        check(_isVaultAuthenticated.value) { "Vault authentication is required." }
        withContext(Dispatchers.IO) {
            vaultFileRepository.clearAll()
            repository.clearAll()
        }
    }

    private fun fetchPublicIp(): String {
        val request = Request.Builder()
            .url("https://api64.ipify.org")
            .get()
            .build()

        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.trim()?.takeIf(::isValidIpAddress)
                } else {
                    null
                }
            } ?: extractLocalIp()
        } catch (_: Exception) {
            extractLocalIp()
        }
    }

    private fun isValidIpAddress(value: String): Boolean {
        if (value.isBlank() || value.length > 45) return false
        if (':' in value) {
            return value.count { it == ':' } >= 2 &&
                value.all { it in "0123456789abcdefABCDEF:" }
        }
        val octets = value.split('.')
        return octets.size == 4 && octets.all { octet ->
            octet.isNotEmpty() && octet.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    private fun extractLocalIp(): String {
        try {
            NetworkInterface.getNetworkInterfaces().toList().mapNotNull { intf ->
                intf.inetAddresses.toList().find { !it.isLoopbackAddress && it is Inet4Address }?.hostAddress
            }.firstOrNull()?.let { return it }
        } catch (_: Exception) {}
        return "N/A"
    }

    private fun monitorConnectivity() {
        if (connectivityCallback != null) return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: android.net.Network, caps: NetworkCapabilities) {
                _connectionType.value = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Connected"
                }
            }

            override fun onLost(network: android.net.Network) {
                _connectionType.value = "DISCONNECTED"
            }
        }
        connectivityCallback = callback
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (_: Exception) {
            connectivityCallback = null
            _connectionType.value = "Unknown"
        }
    }
  
    fun fetchStrategicIntelligence() {
        // This can now be personalized too in the future!
        val list = if (_isArabic.value) {
            listOf("تجنب منافذ الشحن العامة...", "الهندسة الاجتماعية المتقدمة...", "تجنب الاتصال بشبكات الواي فاي...", "امسح فقط الأكواد التفاعلية QR المضمونة...")
        } else {
            listOf("Avoid public charging ports...", "Advanced social engineering vectors...", "Do not connect to secondary public Wi-Fi...", "QR code phishing (Quishing)..." )
        }
        _intelligenceBrief.value = "[!] ALERT: ${list.random()}"
    }

    fun sendTerminalQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isThinking.value = true
            _terminalResponse.value = if (_isArabic.value) "[ جارٍ الاتصال بالنواة العصبية... ]" else "[ CONNECTING TO NEURAL CORE... ]"
            try {
                val sysContext = if (_isArabic.value)
                    "أنت نظام ذكاء اصطناعي للأمن السيبراني باسم A.SYRIA SOVEREIGN OS v4. أجب بأسلوب تقني احترافي وموجز. الاستعلام: $query"
                else
                    "You are A.SYRIA SOVEREIGN OS v4 cybersecurity AI assistant. Respond concisely in professional technical style. Query: $query"
                val result = generateGeminiText(sysContext)
                _terminalResponse.value = result
                _isNeuralLinkOffline.value = false
                addCyberScore(5)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _terminalResponse.value = "[ TERMINAL ALERT ] :Error\n${e.message}"
                _isNeuralLinkOffline.value = true
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun generateAcademyScenarios(
        moduleNameEn: String,
        moduleNameAr: String,
        useArabic: Boolean,
        usedTopics: List<String> = emptyList(),
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isAcademyGenerating.value = true
            val apiKey = _customApiKey.value.ifBlank { _customApiKey.value }
            if (apiKey.isNotBlank()) {
                try {
                    val promptLocale = if (useArabic) "Arabic (العربية)" else "English"
                    val seed = System.currentTimeMillis()

                    // --- NEW ADAPTIVE INTELLIGENCE --- 
                    val level = _userLevel.value
                    val interests = _userInterests.value
                    val goal = _userGoal.value
                    val goalLabel = when (goal) {
                        "self_protect" -> "Protect himself/herself and family from digital threats"
                        "career" -> "Enter the cybersecurity field professionally"
                        "certification" -> "Pass a cybersecurity certification exam"
                        "threat_hunting" -> "Professional threat hunting and red team operations"
                        else -> "General cybersecurity awareness"
                    }
                    val userProfilePrompt = """
                    USER PROFILE:
                    - Skill Level: $level
                    - Core Interests: ${if (interests.isNotEmpty()) interests.joinToString(", ") else "General Cybersecurity"}
                    - Primary Goal: $goalLabel
                    """.trimIndent()

                    val topicFocus = if (interests.isNotEmpty()) interests.random() else "General Cybersecurity"

                    val avoidClause = if (usedTopics.isNotEmpty()) {
                        val topicList = usedTopics.take(30).joinToString("\n- ", prefix = "\n- ")
                        """⛔ STRICTLY FORBIDDEN — Do NOT create any scenario similar to or repeating these already-asked topics:
$topicList

Every scenario you produce MUST be on a COMPLETELY DIFFERENT situation, attack vector, and context from the above list."""
                    } else ""

                    val prompt = """
                        You are a Senior Cybersecurity Trainer AI generating FRESH, UNIQUE quiz scenarios.
                        Entropy seed (guarantees uniqueness): [$seed]
                        Language: $promptLocale
                        Module: '$moduleNameEn' / '$moduleNameAr'

                        $userProfilePrompt

                        Your scenarios MUST be tailored to this user's profile.
                        The difficulty and topic MUST align with their specified level and interests.
                        Topic focus for this round: [$topicFocus]

                        $avoidClause

                        Rules:
                        - Scenarios must match the user's skill level. For "Beginner", use clear, simple language and common threats. For "Advanced", use complex, technical, multi-step attack scenarios.
                        - Scenarios must relate to the topic focus.
                        - Each scenario MUST describe a DIFFERENT real-world attack, threat, or security decision.
                        - Options must be plausible, educational, and distinct from each other.
                        - Explanation must teach a concrete security lesson.
                        - NEVER reuse the same story, platform, or attack method across scenarios.

                        Generate exactly 3 unique challenging scenarios in this STRICT JSON format:
                        {
                          "scenarios": [
                            { "id": "s...", "scenario": "...", "options": [...], "correctIndex": ..., "explanation": "..." }
                          ]
                        }

                        Output ONLY valid JSON. No markdown. No extra text outside JSON.
                    """.trimIndent()
                    
                    val cleanRes = generateGeminiText(prompt).replace("```json", "").replace("```", "").trim()
                    if (cleanRes.isNotBlank()) {
                        onSuccess(cleanRes)
                        _isAcademyGenerating.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    _isAcademyGenerating.value = false
                    onFailure(e)
                    return@launch
                }
            }
            _isAcademyGenerating.value = false
            onFailure(Exception("Neural Link offline or unavailable"))
        }
    }

    fun generateStrategicDebrief(
          scenario: String,
          choiceText: String,
          useArabic: Boolean,
          onResponse: (String) -> Unit
      ) {
          viewModelScope.launch {
              try {
                  val prompt = if (useArabic) {
                      """أنت خبير أمن سيبراني. السيناريو: "$scenario". الاختيار: "$choiceText". قدم تحليلاً استراتيجياً موجزاً لهذا الاختيار وما إذا كان صحيحاً أم لا وكيفية التحسين."""
                  } else {
                      """You are a cybersecurity expert. Scenario: "$scenario". User choice: "$choiceText". Provide a concise strategic debrief of this choice — whether it was correct, why, and how to improve."""
                  }
                  val result = generateGeminiText(prompt)
                  onResponse(result)
              } catch (e: Exception) {
                  if (e is CancellationException) throw e
                  onResponse("")
              }
          }
      }

      fun analyzeResourceLink(url: String) {
        viewModelScope.launch {
            _isAnalyzingLink.value = true
            _linkAnalysisResult.value = null
            try {
                val prompt = if (_isArabic.value) {
                    """أنت محلل أمن سيبراني خبير. حلل هذا الرابط: "$url". قدم: 1. تقييم الأمان (آمن/مشبوه/خطير). 2. تحليل النطاق. 3. توصية للمستخدم. أجب بالعربية بأسلوب موجز."""
                } else {
                    """You are an expert cybersecurity analyst. Analyze this link: "$url". Provide: 1. Security Rating (SAFE/SUSPICIOUS/DANGEROUS). 2. Domain analysis. 3. Actionable user recommendation. Be concise."""
                }
                val result = generateGeminiText(prompt)
                _linkAnalysisResult.value = result
                addCyberScore(10)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _linkAnalysisResult.value = "[ SCAN ERROR ]: ${e.message}"
            } finally {
                _isAnalyzingLink.value = false
            }
        }
    }

    fun clearLinkAnalysis() {
        _linkAnalysisResult.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values
                val (ax, ay) = event.values
                currentRoll += alpha * ((-ax * 4f) - currentRoll)
                currentPitch += alpha * ((ay * 4f) - currentPitch)
                _roll.value = currentRoll
                _pitch.value = currentPitch
            }
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
        }
        if (gravity != null && geomagnetic != null) {
            val rMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rMatrix, null, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rMatrix, orientation)
                _azimuth.value = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        connectivityCallback?.let { callback ->
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
        connectivityCallback = null
        sensorManager.unregisterListener(this)
        try { getApplication<Application>().unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
    }
}
