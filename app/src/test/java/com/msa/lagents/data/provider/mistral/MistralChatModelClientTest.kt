package com.msa.lagents.data.provider.mistral

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

class MistralChatModelClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: MistralChatModelClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = MistralChatModelClient(
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
                    { "id": "mistral-small", "owned_by": "mistral", "capabilities": { "functionCalling": true } },
                    { "id": "mistral-tiny", "owned_by": "mistral", "capabilities": { "functionCalling": false } }
                  ]
                }
            """.trimIndent())
        server.enqueue(mockResponse)

        val models = client.getModels()
        assertEquals(2, models.size)
        assertEquals("mistral-small", models[0].id)
        assertEquals(2, models[0].capabilities.size)
    }

    @Test
    fun `generate streams text deltas`() = runTest {
        val sseContent = "data: {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"},\"finish_reason\":null}]}\n\n" +
                "data: {\"id\":\"2\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\" world\"},\"finish_reason\":null}]}\n\n" +
                "data: [DONE]\n\n"

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/event-stream")
            .setBody(sseContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "mistral-small",
            messages = listOf(ChatMessage(MessageRole.User, "Hi"))
        )

        val events = client.generate(request).toList()
        
        assertEquals(3, events.size)
        assertEquals(GenerationEvent.TextDelta("Hello"), events[0])
        assertEquals(GenerationEvent.TextDelta(" world"), events[1])
        assertEquals(GenerationEvent.Finished, events[2])
    }
}
