package com.example.ui.viewmodel

  import android.app.Application
  import androidx.lifecycle.AndroidViewModel
  import androidx.lifecycle.viewModelScope
  import com.example.data.remote.GeminiRepository
  import com.example.adaptive.UIConfig
  import com.example.data.SovereignDataStore
  import com.example.data.chat.ChatMessage
  import com.example.data.database.AppDatabase
  import kotlinx.coroutines.CancellationException
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.Job
  import kotlinx.coroutines.cancelAndJoin
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.SharingStarted
  import kotlinx.coroutines.flow.StateFlow
  import kotlinx.coroutines.flow.asStateFlow
  import kotlinx.coroutines.flow.first
  import kotlinx.coroutines.flow.stateIn
  import kotlinx.coroutines.launch

  class GeminiChatViewModel(application: Application) : AndroidViewModel(application) {

      private val db         = AppDatabase.getDatabase(application)
      private val dao        = db.chatMessageDao()
      private val dataStore  = SovereignDataStore(application)
      private val gemini       = GeminiRepository()

      val messages: StateFlow<List<ChatMessage>> = dao.observe()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

      private val _isTyping = MutableStateFlow(false)
      val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

      private val _lastError = MutableStateFlow<String?>(null)
      val lastError: StateFlow<String?> = _lastError.asStateFlow()

      private var lastFailedPrompt: String? = null

      private val _geminiKey = MutableStateFlow("")

      private var analysisJob: Job? = null
      private var sendJob: Job? = null
      private var isClearingHistory = false

      init {
          viewModelScope.launch {
              dataStore.migrateLegacyGeminiApiKey()
              dataStore.geminiApiKey.collect { _geminiKey.value = it }
          }
      }

      fun sendMessage(text: String, uiConfig: UIConfig? = null) {
          dispatchMessage(text, uiConfig, persistUserMessage = true)
      }

      /** Retry the last failed prompt without inserting a duplicate user message. */
      fun retryLastMessage(uiConfig: UIConfig? = null) {
          val prompt = lastFailedPrompt ?: return
          dispatchMessage(prompt, uiConfig, persistUserMessage = false)
      }

      private fun dispatchMessage(
          text: String,
          uiConfig: UIConfig?,
          persistUserMessage: Boolean
      ) {
          val trimmed = text.trim()
          if (trimmed.isBlank() || isClearingHistory || _isTyping.value) return

          _isTyping.value = true
          _lastError.value = null
          if (persistUserMessage) lastFailedPrompt = trimmed

          sendJob = viewModelScope.launch(Dispatchers.IO) {
              try {
                  if (persistUserMessage) {
                      dao.insert(ChatMessage(role = "user", content = trimmed))
                  }

                  val key = dataStore.geminiApiKey.first().also { _geminiKey.value = it }
                  if (key.isBlank()) {
                      dao.insert(ChatMessage(
                          role = "assistant",
                          content = "No Gemini API key detected. Head to Settings and add your key to activate me."
                      ))
                      lastFailedPrompt = null
                      return@launch
                  }

                  val history = dao.getRecent(12)
                      .reversed()
                      .filter { it.role == "user" || it.role == "assistant" }
                      .map { it.role to it.content }

                  val reply = gemini.chat(
                      systemPrompt = buildSystemPrompt(uiConfig),
                      history = history,
                      apiKey = key
                  )
                  dao.insert(ChatMessage(role = "assistant", content = reply))
                  lastFailedPrompt = null
              } catch (cancelled: CancellationException) {
                  throw cancelled
              } catch (error: Exception) {
                  _lastError.value = error.message?.take(180)
                      ?: "The request could not be completed. Please try again."
              } finally {
                  _isTyping.value = false
              }
          }
      }

      fun runBackgroundAnalysis(uiConfig: UIConfig) {
          if (isClearingHistory || _isTyping.value || analysisJob?.isActive == true) return
          analysisJob = viewModelScope.launch(Dispatchers.IO) {
              try {
                  val key = dataStore.geminiApiKey.first().also { _geminiKey.value = it }
                  if (key.isBlank()) return@launch

                  val recent = dao.getRecent(6)
                  if (recent.any { it.role == "suggestion" }) return@launch

                  val insight = gemini.generateText(
                      systemInstruction = """You are an intelligent UI advisor embedded in A.SYRIA, a sovereign privacy-first app.
  Analyse the provided configuration and write ONE short, proactive suggestion (2-3 sentences max).
  Start with "I noticed..." and end with a clear, specific question asking if the user wants you to apply the change.
  Be warm, direct, and useful. Plain conversational text only — no JSON, no markdown, no lists.""",
                      input = "Config — accent: ${uiConfig.accentColor}, " +
                          "compact: ${uiConfig.compactMode}, " +
                          "fontSize: ${uiConfig.fontSize}sp, " +
                          "cardStyle: ${uiConfig.cardStyle}, " +
                          "animations: ${uiConfig.animationsEnabled}",
                      apiKey = key
                  )
                  dao.insert(ChatMessage(role = "suggestion", content = insight))
              } catch (cancelled: CancellationException) {
                  throw cancelled
              } catch (_: Exception) {
                  // Best-effort analysis; a later explicit attempt may retry.
              }
          }
      }

      fun clearHistory() {
          if (isClearingHistory) return
          isClearingHistory = true
          _lastError.value = null
          lastFailedPrompt = null
          val pendingSend = sendJob
          val pendingAnalysis = analysisJob
          sendJob = null
          analysisJob = null

          viewModelScope.launch(Dispatchers.IO) {
              try {
                  pendingSend?.cancelAndJoin()
                  pendingAnalysis?.cancelAndJoin()
                  dao.clear()
              } finally {
                  _isTyping.value = false
                  isClearingHistory = false
              }
          }
      }

      private fun buildSystemPrompt(uiConfig: UIConfig?): String {
          val config = uiConfig?.let {
              "\nActive UI config — accent: ${it.accentColor}, compact: ${it.compactMode}, " +
              "fontSize: ${it.fontSize}sp, cardStyle: ${it.cardStyle}"
          }.orEmpty()

          return "You are the Master Brain of A.SYRIA — a sovereign, privacy-first Android app for Syrians and Arabic-speaking users.\n" +
              "Powered by Gemini 3.6 Flash. You are articulate, precise, and deeply helpful.\n\n" +
              "You can:\n" +
              "  • Customise the app UI (colors, fonts, layout, animations) by describing changes\n" +
              "  • Answer questions about app features, privacy, and cybersecurity\n" +
              "  • Converse naturally in English or Arabic — always reply in the user's language\n" +
              "$config\n\n" +
              "Keep responses focused and under 180 words unless detail is specifically requested."
      }
  }
  