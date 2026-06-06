package com.msa.lagents.data.provider.gemini

import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiChatModelClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: GeminiChatModelClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = GeminiChatModelClient(
            httpClient = OkHttpClient(),
            apiKey = "test-key",
            baseUrl = server.url("/v1beta").toString()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `generate streams text deltas from JSON array`() = runTest {
        val jsonArrayContent = """
            [
              {
                "candidates": [{ "content": { "parts": [{ "text": "Hello" }] }, "index": 0 }]
              },
              {
                "candidates": [{ "content": { "parts": [{ "text": " world" }] }, "index": 0 }],
                "usageMetadata": { "promptTokenCount": 5, "candidatesTokenCount": 2, "totalTokenCount": 7 }
              }
            ]
        """.trimIndent()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonArrayContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "gemini-1.5-pro",
            messages = listOf(ChatMessage(MessageRole.User, "Hi"))
        )

        val events = client.generate(request).toList()
        
        assertEquals(4, events.size)
        assertEquals(GenerationEvent.TextDelta("Hello"), events[0])
        assertEquals(GenerationEvent.TextDelta(" world"), events[1])
        assertTrue(events[2] is GenerationEvent.Usage)
        assertEquals(GenerationEvent.Finished, events[3])
    }

    @Test
    fun `generate handles tool calls from Gemini`() = runTest {
        val jsonArrayContent = """
            [
              {
                "candidates": [{
                  "content": {
                    "parts": [{
                      "functionCall": {
                        "name": "get_weather",
                        "args": { "city": "Paris" }
                      }
                    }]
                  },
                  "index": 0
                }]
              }
            ]
        """.trimIndent()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonArrayContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "gemini-1.5-pro",
            messages = listOf(ChatMessage(MessageRole.User, "Weather in Paris?"))
        )

        val events = client.generate(request).toList()

        assertEquals(2, events.size)
        val toolDelta = events[0] as GenerationEvent.ToolCallDelta
        assertEquals("get_weather", toolDelta.name)
        assertTrue(toolDelta.argumentsDelta!!.contains("Paris"))
        assertEquals(GenerationEvent.Finished, events[1])
    }
}
