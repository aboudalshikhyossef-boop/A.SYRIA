package com.example.adaptive

  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.withContext
  import okhttp3.MediaType.Companion.toMediaType
  import okhttp3.OkHttpClient
  import okhttp3.Request
  import okhttp3.RequestBody.Companion.toRequestBody
  import org.json.JSONArray
  import org.json.JSONObject
  import java.util.concurrent.TimeUnit

  class GroqRepository {

      private val client = OkHttpClient.Builder()
          .connectTimeout(20, TimeUnit.SECONDS)
          .readTimeout(30, TimeUnit.SECONDS)
          .build()

      private companion object {
          const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
          const val MODEL    = "llama-3.3-70b-versatile"
      }

      /**
       * Single-turn completion — used by FeedbackService for UI customisation.
       */
      suspend fun complete(
          systemPrompt: String,
          userContent: String,
          groqApiKey: String
      ): String = withContext(Dispatchers.IO) {
          val body = JSONObject().apply {
              put("model", MODEL)
              put("temperature", 0.2)
              put("messages", JSONArray().apply {
                  put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
                  put(JSONObject().apply { put("role", "user");   put("content", userContent) })
              })
          }.toString()
          execute(body, groqApiKey)
      }

      /**
       * Multi-turn chat — used by GroqChatViewModel for conversational sessions.
       * [history] is a list of (role, content) pairs in chronological order.
       */
      suspend fun chat(
          systemPrompt: String,
          history: List<Pair<String, String>>,
          groqApiKey: String
      ): String = withContext(Dispatchers.IO) {
          val body = JSONObject().apply {
              put("model", MODEL)
              put("temperature", 0.7)
              put("max_tokens", 600)
              put("messages", JSONArray().apply {
                  put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
                  history.forEach { (role, content) ->
                      val apiRole = if (role.trim() == "assistant") "assistant" else "user"
                      put(JSONObject().apply { put("role", apiRole); put("content", content) })
                  }
              })
          }.toString()
          execute(body, groqApiKey)
      }

      private fun execute(requestBody: String, groqApiKey: String): String {
          val request = Request.Builder()
              .url(ENDPOINT)
              .post(requestBody.toRequestBody("application/json".toMediaType()))
              .header("Authorization", "Bearer $groqApiKey")
              .header("Content-Type", "application/json")
              .build()

          val response = client.newCall(request).execute()
          val rawBody  = response.body?.string() ?: throw Exception("Empty response from Groq.")

          if (!response.isSuccessful) {
              val msg = try {
                  JSONObject(rawBody).optJSONObject("error")?.optString("message") ?: "Groq error"
              } catch (_: Exception) { rawBody.take(200) }
              throw Exception("Groq ${response.code}: $msg")
          }

          return JSONObject(rawBody)
              .getJSONArray("choices")
              .getJSONObject(0)
              .getJSONObject("message")
              .getString("content")
              .trim()
      }
  }
  