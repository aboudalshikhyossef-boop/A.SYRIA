package com.example.adaptive

  import android.content.Context
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.withContext
  import org.json.JSONArray

  sealed class FeedbackResult {
      data class Success(val changes: List<UIChange>) : FeedbackResult()
      data class RateLimited(val resetSeconds: Long, val remaining: Int) : FeedbackResult()
      data class Empty(val message: String) : FeedbackResult()
      data class Error(val message: String) : FeedbackResult()
  }

  /**
   * FeedbackService — Orchestrates UI-customisation requests through the Groq layer.
   *
   * This service is intentionally decoupled from Gemini / Google AI.
   * All LLM calls go through [GroqRepository] using the user's dedicated Groq API key.
   * Gemini remains exclusively responsible for Quiz & Test generation (DashboardViewModel).
   */
  class FeedbackService(private val context: Context) {

      private val groqRepository = GroqRepository()

      /**
       * Submit a UI-customisation request.
       *
       * @param feedbackText  Raw user input (e.g. "make it green", "bigger text").
       * @param groqApiKey    The user's Groq API key loaded from DataStore.
       * @return [FeedbackResult.Success], [RateLimited], [Empty], or [Error].
       */
      suspend fun submitFeedback(feedbackText: String, groqApiKey: String): FeedbackResult =
          withContext(Dispatchers.IO) {

              // Rate-limit guard
              if (!RateLimiter.isAllowed(context)) {
                  return@withContext FeedbackResult.RateLimited(
                      resetSeconds = RateLimiter.getResetSeconds(context),
                      remaining    = 0
                  )
              }

              // Groq key validation
              if (groqApiKey.isBlank()) {
                  return@withContext FeedbackResult.Error(
                      if (context.resources.configuration.locales[0].language == "ar")
                          "مفتاح Groq API غير متوفر. أضفه في الإعدادات أولاً."
                      else
                          "Groq API key not configured. Add your key in Settings first."
                  )
              }

              // Sanitise: strip JSON-breaking chars, cap at 500 chars
              val sanitized = feedbackText
                  .replace(Regex("[{}\\[\\]]"), "")
                  .take(500)
                  .trim()

              if (sanitized.isBlank()) {
                  return@withContext FeedbackResult.Error("Input is empty.")
              }

              try {
                  val aiText = groqRepository.complete(
                      systemPrompt = buildSystemPrompt(),
                      userContent  = sanitized,
                      groqApiKey   = groqApiKey
                  )

                  val changes = parseAndValidateChanges(aiText)
                  RateLimiter.recordRequest(context)

                  if (changes.isEmpty()) {
                      FeedbackResult.Empty(
                          if (context.resources.configuration.locales[0].language == "ar")
                              "لم يتم التعرف على تغييرات. حاول: 'لون أخضر'، 'خط أكبر'، 'وضع مضغوط'."
                          else
                              "No changes detected. Try: 'green color', 'bigger text', 'compact mode'."
                      )
                  } else {
                      FeedbackResult.Success(changes)
                  }

              } catch (e: Exception) {
                  FeedbackResult.Error("Connection error: ${e.message?.take(100)}")
              }
          }

      // ── Private helpers ──────────────────────────────────────────────────────

      private fun buildSystemPrompt(): String {
          val configKeys = """
  accentColor: cyan|amber|green|white|red|purple
  primaryHex: hex color e.g. #FF5500
  backgroundHex: hex background e.g. #0D1117
  fontSize: 10 to 24
  fontWeight: light|normal|bold
  cornerRadius: 0 to 32
  cardStyle: glass|solid|outline|flat
  compactMode: true|false
  tabStyle: compact|normal
  showPrayerTab: true|false
  showAcademyTab: true|false
  showStatusBar: true|false
  animationsEnabled: true|false
  notificationLevel: silent|normal|active
  is12HourFormat: true|false""".trimIndent()

          return """
  You are a UI configuration assistant for the A.SYRIA Sovereign OS app.
  Translate the user's natural-language request into a JSON array of UI-change objects.
  Output ONLY a valid JSON array — no prose, no markdown, no code fences.

  Available configuration keys:
  ${configKeys}

  Each array element MUST have exactly these four fields:
  {"key":"<config key>","value":"<new value>","label":"<Arabic — English>","preview":"<one-line description>"}

  Examples:
  - 'green' -> [{"key":"accentColor","value":"green","label":"لون أخضر — Green Accent","preview":"Changes accent color to green"}]
  - 'bigger text' -> [{"key":"fontSize","value":"18","label":"خط أكبر — Larger Font","preview":"Increases text size to 18sp"}]
  - '12 hour clock' -> [{"key":"is12HourFormat","value":"true","label":"توقيت 12 ساعة — 12h Clock","preview":"Switches clock to AM/PM format"}]
  - 'compact dark' -> [{"key":"compactMode","value":"true","label":"وضع مضغوط — Compact Mode","preview":"Enables compact layout"},{"key":"backgroundHex","value":"#070D14","label":"خلفية داكنة — Dark Background","preview":"Sets a very dark background"}]
  """.trimIndent()
      }

      private fun parseAndValidateChanges(aiText: String): List<UIChange> {
          val cleaned = aiText
              .replace(Regex("^```[a-z]*\\n?", RegexOption.MULTILINE), "")
              .replace("```", "")
              .trim()

          return try {
              val arr = JSONArray(cleaned)
              (0 until arr.length()).mapNotNull { i ->
                  val obj     = arr.getJSONObject(i)
                  val key     = obj.optString("key").trim()
                  val value   = obj.optString("value").trim()
                  val label   = obj.optString("label", key)
                  val preview = obj.optString("preview", "")

                  val validator = AdaptiveEngine.ALLOWED_KEYS[key] ?: return@mapNotNull null
                  if (!validator(value)) return@mapNotNull null

                  UIChange(key = key, value = value, label = label, preview = preview)
              }
          } catch (_: Exception) { emptyList() }
      }
  }
  