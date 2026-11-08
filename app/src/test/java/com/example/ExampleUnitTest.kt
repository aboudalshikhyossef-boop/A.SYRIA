package com.example

import com.example.data.remote.GeminiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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
}
