package com.msa.lagents.data.provider.anthropic

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

class AnthropicChatModelClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: AnthropicChatModelClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = AnthropicChatModelClient(
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
    fun `generate streams text deltas`() = runTest {
        val sseContent = "event: message_start\n" +
                "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[],\"model\":\"claude-3-sonnet\",\"stop_reason\":null,\"stop_sequence\":null,\"usage\":{\"input_tokens\":10,\"output_tokens\":1}}}\n\n" +
                "event: content_block_start\n" +
                "data: {\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"text\",\"text\":\"\"}}\n\n" +
                "event: content_block_delta\n" +
                "data: {\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\"Hello\"}}\n\n" +
                "event: content_block_delta\n" +
                "data: {\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\" world\"}}\n\n" +
                "event: message_delta\n" +
                "data: {\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"end_turn\",\"stop_sequence\":null},\"usage\":{\"output_tokens\":15}}\n\n" +
                "event: message_stop\n" +
                "data: {\"type\":\"message_stop\"}\n\n"

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/event-stream")
            .setBody(sseContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "claude-3-sonnet",
            messages = listOf(ChatMessage(MessageRole.User, "Hi"))
        )

        val events = client.generate(request).toList()
        
        assertEquals(5, events.size)
        assertTrue(events[0] is GenerationEvent.Usage)
        assertEquals(GenerationEvent.TextDelta("Hello"), events[1])
        assertEquals(GenerationEvent.TextDelta(" world"), events[2])
        assertTrue(events[3] is GenerationEvent.Usage)
        assertEquals(GenerationEvent.Finished, events[4])
    }

    @Test
    fun `generate handles tool call deltas`() = runTest {
        val sseContent = "event: message_start\n" +
                "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[],\"model\":\"claude-3-sonnet\",\"stop_reason\":null,\"stop_sequence\":null,\"usage\":{\"input_tokens\":10,\"output_tokens\":1}}}\n\n" +
                "event: content_block_start\n" +
                "data: {\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"tool_use\",\"id\":\"call_1\",\"name\":\"get_weather\",\"input\":{}}}\n\n" +
                "event: content_block_delta\n" +
                "data: {\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"city\\\"\"}}\n\n" +
                "event: content_block_delta\n" +
                "data: {\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\":\\\"London\\\"}\"}}\n\n" +
                "event: message_stop\n" +
                "data: {\"type\":\"message_stop\"}\n\n"

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/event-stream")
            .setBody(sseContent)
        server.enqueue(mockResponse)

        val request = GenerationRequest(
            modelId = "claude-3-sonnet",
            messages = listOf(ChatMessage(MessageRole.User, "Weather in London?"))
        )

        val events = client.generate(request).toList()

        assertEquals(5, events.size)
        assertTrue(events[0] is GenerationEvent.Usage)
        
        val toolStart = events[1] as GenerationEvent.ToolCallDelta
        assertEquals("call_1", toolStart.id)
        assertEquals("get_weather", toolStart.name)

        val toolDelta1 = events[2] as GenerationEvent.ToolCallDelta
        assertEquals("{\"city\"", toolDelta1.argumentsDelta)

        val toolDelta2 = events[3] as GenerationEvent.ToolCallDelta
        assertEquals(":\"London\"}", toolDelta2.argumentsDelta)
        
        assertEquals(GenerationEvent.Finished, events[4])
    }
}
