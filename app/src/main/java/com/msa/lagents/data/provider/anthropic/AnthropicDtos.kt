package com.msa.lagents.data.provider.anthropic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AnthropicMessageRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    @SerialName("max_tokens") val maxTokens: Int,
    val stream: Boolean = false,
    val tools: List<AnthropicTool>? = null,
    val temperature: Float? = null,
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: List<AnthropicContentBlock>,
)

@Serializable(with = AnthropicContentBlockSerializer::class)
sealed class AnthropicContentBlock {
    @Serializable
    data class Text(val type: String = "text", val text: String) : AnthropicContentBlock()

    @Serializable
    data class ToolUse(
        val type: String = "tool_use",
        val id: String,
        val name: String,
        val input: JsonObject,
    ) : AnthropicContentBlock()

    @Serializable
    data class ToolResult(
        val type: String = "tool_result",
        @SerialName("tool_use_id") val toolUseId: String,
        val content: String,
    ) : AnthropicContentBlock()
}

object AnthropicContentBlockSerializer : JsonContentPolymorphicSerializer<AnthropicContentBlock>(AnthropicContentBlock::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"]?.jsonPrimitive?.content) {
        "text" -> AnthropicContentBlock.Text.serializer()
        "tool_use" -> AnthropicContentBlock.ToolUse.serializer()
        "tool_result" -> AnthropicContentBlock.ToolResult.serializer()
        else -> throw Exception("Unknown content block type")
    }
}

@Serializable
data class AnthropicTool(
    val name: String,
    val description: String,
    @SerialName("input_schema") val inputSchema: JsonObject,
)

@Serializable(with = AnthropicStreamEventSerializer::class)
sealed class AnthropicStreamEvent {
    @Serializable
    data class MessageStart(val type: String, val message: AnthropicMessageResponse) : AnthropicStreamEvent()

    @Serializable
    data class ContentBlockStart(
        val type: String,
        val index: Int,
        @SerialName("content_block") val contentBlock: AnthropicContentBlock,
    ) : AnthropicStreamEvent()

    @Serializable
    data class ContentBlockDelta(
        val type: String,
        val index: Int,
        val delta: AnthropicDelta,
    ) : AnthropicStreamEvent()

    @Serializable
    data class MessageDelta(
        val type: String,
        val delta: AnthropicMessageDeltaContent,
        val usage: AnthropicUsage? = null,
    ) : AnthropicStreamEvent()

    @Serializable
    data class MessageStop(val type: String) : AnthropicStreamEvent()

    @Serializable
    data class Error(val type: String, val error: AnthropicError) : AnthropicStreamEvent()
}

object AnthropicStreamEventSerializer : JsonContentPolymorphicSerializer<AnthropicStreamEvent>(AnthropicStreamEvent::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"]?.jsonPrimitive?.content) {
        "message_start" -> AnthropicStreamEvent.MessageStart.serializer()
        "content_block_start" -> AnthropicStreamEvent.ContentBlockStart.serializer()
        "content_block_delta" -> AnthropicStreamEvent.ContentBlockDelta.serializer()
        "message_delta" -> AnthropicStreamEvent.MessageDelta.serializer()
        "message_stop" -> AnthropicStreamEvent.MessageStop.serializer()
        "error" -> AnthropicStreamEvent.Error.serializer()
        else -> throw Exception("Unknown stream event type")
    }
}

@Serializable
data class AnthropicMessageResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContentBlock>,
    val model: String,
    @SerialName("stop_reason") val stopReason: String?,
    @SerialName("stop_sequence") val stopSequence: String?,
    val usage: AnthropicUsage,
)

@Serializable
data class AnthropicDelta(
    val type: String,
    val text: String? = null,
    @SerialName("partial_json") val partialJson: String? = null,
)

@Serializable
data class AnthropicMessageDeltaContent(
    @SerialName("stop_reason") val stopReason: String?,
    @SerialName("stop_sequence") val stopSequence: String?,
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
)

@Serializable
data class AnthropicError(
    val type: String,
    val message: String,
)
