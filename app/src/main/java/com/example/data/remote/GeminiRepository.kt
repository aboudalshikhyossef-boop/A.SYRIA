package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Single entry point for every Gemini request in the app.
 *
 * Uses Google's Interactions API and opts out of server-side conversation
 * storage. Conversation history is kept locally by the app.
 */
class GeminiRepository(
    private val client: OkHttpClient = defaultClient()
) {
    suspend fun generateText(
        systemInstruction: String,
        input: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Gemini API key is missing." }
        require(input.isNotBlank()) { "Gemini input is empty." }

        val payload = JSONObject().apply {
            put("model", MODEL_ID)
            put("system_instruction", systemInstruction)
            put("input", input)
            put("store", false)
        }

        val request = Request.Builder()
            .url(ENDPOINT)
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    JSONObject(body).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty().ifBlank { "Gemini request failed." }
                throw GeminiApiException(response.code, message)
            }
            extractOutputText(body).ifBlank {
                throw IllegalStateException("Gemini returned an empty response.")
            }
        }
    }

    suspend fun chat(
        systemPrompt: String,
        history: List<Pair<String, String>>,
        apiKey: String
    ): String {
        val conversation = buildString {
            history.takeLast(MAX_HISTORY_MESSAGES).forEach { (role, content) ->
                val label = if (role.equals("assistant", ignoreCase = true)) "Assistant" else "User"
                append(label).append(":\n").append(content.trim()).append("\n\n")
            }
        }.trim()

        return generateText(
            systemInstruction = systemPrompt,
            input = conversation,
            apiKey = apiKey
        )
    }

    companion object {
        const val MODEL_ID = "gemini-3.6-flash"
        const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/interactions"
        private const val MAX_HISTORY_MESSAGES = 20
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        internal fun extractOutputText(responseBody: String): String {
            val root = JSONObject(responseBody)
            root.optString("output_text").takeIf { it.isNotBlank() }?.let { return it.trim() }

            val steps = root.optJSONArray("steps") ?: root.optJSONArray("output") ?: JSONArray()
            val text = StringBuilder()
            for (index in 0 until steps.length()) {
                val step = steps.optJSONObject(index) ?: continue
                val type = step.optString("type")
                if (type.isNotBlank() && type != "model_output") continue

                val content = step.optJSONArray("content") ?: continue
                for (contentIndex in 0 until content.length()) {
                    val part = content.optJSONObject(contentIndex) ?: continue
                    if (part.optString("type") == "text") {
                        part.optString("text").takeIf { it.isNotBlank() }?.let {
                            if (text.isNotEmpty()) text.append('\n')
                            text.append(it)
                        }
                    }
                }
            }
            return text.toString().trim()
        }

        private fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }
}

class GeminiApiException(
    val statusCode: Int,
    message: String
) : Exception("Gemini API $statusCode: $message")
