package com.example.ui.viewmodel

  import android.app.Application
  import androidx.lifecycle.AndroidViewModel
  import androidx.lifecycle.viewModelScope
  import com.example.adaptive.GroqRepository
  import com.example.adaptive.UIConfig
  import com.example.data.SovereignDataStore
  import com.example.data.chat.ChatMessage
  import com.example.data.database.AppDatabase
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.SharingStarted
  import kotlinx.coroutines.flow.StateFlow
  import kotlinx.coroutines.flow.asStateFlow
  import kotlinx.coroutines.flow.stateIn
  import kotlinx.coroutines.launch

  class GroqChatViewModel(application: Application) : AndroidViewModel(application) {

      private val db         = AppDatabase.getDatabase(application)
      private val dao        = db.chatMessageDao()
      private val dataStore  = SovereignDataStore(application)
      private val groq       = GroqRepository()

      val messages: StateFlow<List<ChatMessage>> = dao.observe()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

      private val _isTyping = MutableStateFlow(false)
      val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

      private val _groqKey = MutableStateFlow("")

      private var analysisScheduled = false

      init {
          viewModelScope.launch {
              dataStore.groqApiKey.collect { _groqKey.value = it }
          }
      }

      fun sendMessage(text: String, uiConfig: UIConfig? = null) {
          val trimmed = text.trim()
          if (trimmed.isBlank()) return

          viewModelScope.launch(Dispatchers.IO) {
              dao.insert(ChatMessage(role = "user", content = trimmed))
              _isTyping.value = true

              try {
                  val key = _groqKey.value
                  if (key.isBlank()) {
                      dao.insert(ChatMessage(
                          role = "assistant",
                          content = "No Groq API key detected. Head to Settings and add your key to activate me."
                      ))
                      return@launch
                  }

                  val history = dao.getRecent(12)
                      .reversed()
                      .filter { it.role == "user" || it.role == "assistant" }
                      .map { it.role to it.content }

                  val reply = groq.chat(
                      systemPrompt = buildSystemPrompt(uiConfig),
                      history      = history,
                      groqApiKey   = key
                  )
                  dao.insert(ChatMessage(role = "assistant", content = reply))

              } catch (e: Exception) {
                  dao.insert(ChatMessage(
                      role    = "assistant",
                      content = "Connection error — ${e.message?.take(90) ?: "unknown"}"
                  ))
              } finally {
                  _isTyping.value = false
              }
          }
      }

      fun runBackgroundAnalysis(uiConfig: UIConfig) {
          if (analysisScheduled) return
          analysisScheduled = true

          viewModelScope.launch(Dispatchers.IO) {
              val key = _groqKey.value
              if (key.isBlank()) return@launch

              val recent = dao.getRecent(6)
              if (recent.any { it.role == "suggestion" }) return@launch

              try {
                  val insight = groq.complete(
                      systemPrompt = """You are an intelligent UI advisor embedded in A.SYRIA, a sovereign privacy-first app.
  Analyse the provided configuration and write ONE short, proactive suggestion (2-3 sentences max).
  Start with "I noticed..." and end with a clear, specific question asking if the user wants you to apply the change.
  Be warm, direct, and useful. Plain conversational text only — no JSON, no markdown, no lists.""",
                      userContent = "Config — accent: ${uiConfig.accentColor}, " +
                          "compact: ${uiConfig.compactMode}, " +
                          "fontSize: ${uiConfig.fontSize}sp, " +
                          "cardStyle: ${uiConfig.cardStyle}, " +
                          "animations: ${uiConfig.animationsEnabled}",
                      groqApiKey = key
                  )
                  dao.insert(ChatMessage(role = "suggestion", content = insight))
              } catch (_: Exception) { /* Silent — background analysis is best-effort */ }
          }
      }

      fun clearHistory() {
          viewModelScope.launch(Dispatchers.IO) {
              dao.clear()
              analysisScheduled = false
          }
      }

      private fun buildSystemPrompt(uiConfig: UIConfig?): String {
          val config = uiConfig?.let {
              "\nActive UI config — accent: ${it.accentColor}, compact: ${it.compactMode}, " +
              "fontSize: ${it.fontSize}sp, cardStyle: ${it.cardStyle}"
          }.orEmpty()

          return "You are the Master Brain of A.SYRIA — a sovereign, privacy-first Android app for Syrians and Arabic-speaking users.\n" +
              "Powered by LLaMA 3.3 70B via Groq. You are articulate, precise, and deeply helpful.\n\n" +
              "You can:\n" +
              "  • Customise the app UI (colors, fonts, layout, animations) by describing changes\n" +
              "  • Answer questions about app features, privacy, and cybersecurity\n" +
              "  • Converse naturally in English or Arabic — always reply in the user's language\n" +
              "$config\n\n" +
              "Keep responses focused and under 180 words unless detail is specifically requested."
      }
  }
  