package com.msa.lagents.data.provider.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val stream: Boolean = false,
    val tools: List<OpenAiTool>? = null,
    val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiToolCall>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
)

@Serializable
data class OpenAiTool(
    val type: String = "function",
    val function: OpenAiFunctionDefinition,
)

@Serializable
data class OpenAiFunctionDefinition(
    val name: String,
    val description: String,
    val parameters: kotlinx.serialization.json.JsonObject,
)

@Serializable
data class OpenAiToolCall(
    val id: String,
    val type: String,
    val function: OpenAiFunctionCall,
)

@Serializable
data class OpenAiFunctionCall(
    val name: String,
    val arguments: String,
)

@Serializable
data class OpenAiChatChunk(
    val id: String,
    val choices: List<OpenAiChunkChoice>,
    val usage: OpenAiUsage? = null,
)

@Serializable
data class OpenAiChunkChoice(
    val delta: OpenAiDelta,
    @SerialName("finish_reason") val finishReason: String?,
    val index: Int,
)

@Serializable
data class OpenAiDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiDeltaToolCall>? = null,
)

@Serializable
data class OpenAiDeltaToolCall(
    val index: Int,
    val id: String? = null,
    val function: OpenAiDeltaFunction? = null,
)

@Serializable
data class OpenAiDeltaFunction(
    val name: String? = null,
    val arguments: String? = null,
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
)

@Serializable
data class OpenAiModelsResponse(
    val data: List<OpenAiModel>,
)

@Serializable
data class OpenAiModel(
    val id: String,
    val ownedBy: String? = null,
)
