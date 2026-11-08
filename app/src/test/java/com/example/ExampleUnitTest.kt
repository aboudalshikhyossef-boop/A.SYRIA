package com.example

import android.app.Application
import android.content.Context
import com.example.adaptive.AdaptiveEngine
import com.example.data.remote.GeminiRepository
import com.example.ui.screens.loadLibraryData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun usesThePinnedGeminiModel() {
        assertEquals("gemini-3.6-flash", GeminiRepository.MODEL_ID)
    }

    @Test
    fun usesTheInteractionsEndpoint() {
        assertTrue(GeminiRepository.ENDPOINT.endsWith("/v1beta/interactions"))
    }

    @Test
    fun extractsConvenienceOutputText() {
        val response = """{"output_text":"Hello from Gemini"}"""
        assertEquals("Hello from Gemini", GeminiRepository.extractOutputText(response))
    }

    @Test
    fun extractsTextFromModelOutputStepsAndIgnoresToolSteps() {
        val response = """
            {
              "steps": [
                {"type":"function_call","content":[{"type":"text","text":"not user output"}]},
                {"type":"model_output","content":[
                  {"type":"text","text":"First part."},
                  {"type":"image","uri":"ignored"},
                  {"type":"text","text":"Second part."}
                ]}
              ]
            }
        """.trimIndent()

        assertEquals(
            "First part.\nSecond part.",
            GeminiRepository.extractOutputText(response)
        )
    }

    @Test
    fun extractsTextFromOutputArrayFallback() {
        val response = """
            {
              "output": [
                {"type":"model_output","content":[{"type":"text","text":"Fallback text"}]}
              ]
            }
        """.trimIndent()

        assertEquals("Fallback text", GeminiRepository.extractOutputText(response))
    }

    @Test
    fun libraryLoadsOnlyUniqueDuasAndHadiths() {
        val context = RuntimeEnvironment.getApplication<Application>()
        val data = loadLibraryData(context)

        assertEquals(setOf("duas", "hadiths"), data.keys)
        assertEquals(8, data["duas"]?.size)
        assertEquals(8, data["hadiths"]?.size)
        assertTrue(data.values.flatten().all { it.body.isNotBlank() })
    }

    @Test
    fun invalidPersistedUiConfigFallsBackToSafeValues() {
        val context = RuntimeEnvironment.getApplication<Application>()
        val preferences = context.getSharedPreferences(
            "sovereign_adaptive_ui",
            Context.MODE_PRIVATE
        )
        preferences.edit().putString(
            "ui_config_json_v3",
            """{
                "accentColor":"not-a-color",
                "primaryHex":"javascript:alert(1)",
                "backgroundHex":"#0D1117",
                "fontSize":999,
                "fontWeight":"ultra",
                "cornerRadius":99,
                "cardStyle":"injected",
                "tabStyle":"unknown",
                "notificationLevel":"LOUD"
            }"""
        ).commit()

        try {
            val config = AdaptiveEngine(context).uiConfig.value
            assertEquals("cyan", config.accentColor)
            assertEquals("", config.primaryHex)
            assertEquals("#0D1117", config.backgroundHex)
            assertEquals(14f, config.fontSize, 0f)
            assertEquals("normal", config.fontWeight)
            assertEquals(32, config.cornerRadius)
            assertEquals("glass", config.cardStyle)
            assertEquals("normal", config.tabStyle)
            assertEquals("normal", config.notificationLevel)
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
