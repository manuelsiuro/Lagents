package com.msa.lagents.data.provider.openai

import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import com.msa.lagents.domain.model.ProviderError
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

class OpenAiChatModelClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OpenAiChatModelClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = OpenAiChatModelClient(
            httpClient = OkHttpClient(),
            apiKey = "test-key",
            baseUrl = server.url("/v1").toString()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getModels parses response correctly`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "data": [
                    { "id": "gpt-4o", "owned_by": "openai" },
                    { "id": "gpt-4o-mini", "owned_by": "system" }
                  ]
                }
            """.trimIndent())
        server.enqueue(mockResponse)

        val models = client.getModels()
        assertEquals(2, models.size)
        assertEquals("gpt-4o", models[0].id)
        assertEquals("gpt-4o-mini", models[1].id)
    }

    @Test
    fun `generate streams text deltas`() = runTest {
        val sseContent = """
            data: {"id":"chatcmpl-123","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}
            
            data: {"id":"chatcmpl-123","choices":[{"index":0,"delta":{"content":" world"},"finish_reason":null}]}
            
            data: [DONE]
            
        """.trimIndent()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/event-stream")
            .setBody(sseContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "gpt-4o",
            messages = listOf(ChatMessage(MessageRole.User, "Hi"))
        )

        val events = client.generate(request).toList()
        
        assertEquals(3, events.size)
        assertEquals(GenerationEvent.TextDelta("Hello"), events[0])
        assertEquals(GenerationEvent.TextDelta(" world"), events[1])
        assertEquals(GenerationEvent.Finished, events[2])
    }

    @Test
    fun `generate handles handles tool call deltas`() = runTest {
        val sseContent = """
            data: {"id":"chatcmpl-123","choices":[{"index":0,"delta":{"tool_calls":[{"index":0,"id":"call_1","function":{"name":"get_weather","arguments":""}}]},"finish_reason":null}]}
            
            data: {"id":"chatcmpl-123","choices":[{"index":0,"delta":{"tool_calls":[{"index":0,"function":{"arguments":"{\"city\":\"London\"}"}}]},"finish_reason":null}]}
            
            data: [DONE]
            
        """.trimIndent()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/event-stream")
            .setBody(sseContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "gpt-4o",
            messages = listOf(ChatMessage(MessageRole.User, "Weather in London?"))
        )

        val events = client.generate(request).toList()

        assertEquals(3, events.size)
        val toolDelta1 = events[0] as GenerationEvent.ToolCallDelta
        assertEquals("call_1", toolDelta1.id)
        assertEquals("get_weather", toolDelta1.name)

        val toolDelta2 = events[1] as GenerationEvent.ToolCallDelta
        assertEquals("{\"city\":\"London\"}", toolDelta2.argumentsDelta)
        assertEquals(GenerationEvent.Finished, events[2])
    }

    @Test
    fun `generate handles authentication error`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody("{\"error\": {\"message\": \"Invalid API key\"}}")
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "gpt-4o",
            messages = listOf(ChatMessage(MessageRole.User, "Hi"))
        )

        val events = client.generate(request).toList()
        
        assertTrue(events[0] is GenerationEvent.Error)
        val error = (events[0] as GenerationEvent.Error).error
        assertTrue(error is ProviderError.Authentication)
    }
}
