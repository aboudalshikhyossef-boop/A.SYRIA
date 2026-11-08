package com.example

import com.example.data.remote.GeminiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun usesThePinnedGeminiModel() {
        assertEquals("gemini-3.6-flash", GeminiRepository.MODEL_ID)
    }

    @Test
    fun usesTheInteractionsEndpoint() {
        assertTrue(GeminiRepository.ENDPOINT.endsWith("/v1beta/interactions"))
    }
}
