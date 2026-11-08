package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.GeminiRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readsApplicationName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("A.SYRIA", context.getString(R.string.app_name))
    }

    @Test
    fun extractsGeminiOutputText() {
        val response = """{"output_text":"Gemini is ready."}"""
        assertEquals("Gemini is ready.", GeminiRepository.extractOutputText(response))
    }

    @Test
    fun extractsTextFromInteractionSteps() {
        val response = """{"steps":[{"type":"model_output","content":[{"type":"text","text":"First line"},{"type":"text","text":"Second line"}]}]}"""
        assertEquals(
            "First line\nSecond line",
            GeminiRepository.extractOutputText(response)
        )
    }
}
