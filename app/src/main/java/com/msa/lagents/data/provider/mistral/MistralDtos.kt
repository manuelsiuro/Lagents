package com.msa.lagents.data.provider.mistral

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MistralChatRequest(
    val model: String,
    val messages: List<MistralMessage>,
    val stream: Boolean = false,
    val tools: List<MistralTool>? = null,
    val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
)

@Serializable
data class MistralMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<MistralToolCall>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
)

@Serializable
data class MistralTool(
    val type: String = "function",
    val function: MistralFunctionDefinition,
)

@Serializable
data class MistralFunctionDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
data class MistralToolCall(
    val id: String,
    val type: String,
    val function: MistralFunctionCall,
)

@Serializable
data class MistralFunctionCall(
    val name: String,
    val arguments: String,
)

@Serializable
data class MistralChatChunk(
    val id: String,
    val choices: List<MistralChunkChoice>,
    val usage: MistralUsage? = null,
)

@Serializable
data class MistralChunkChoice(
    val delta: MistralDelta,
    @SerialName("finish_reason") val finishReason: String?,
    val index: Int,
)

@Serializable
data class MistralDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<MistralDeltaToolCall>? = null,
)

@Serializable
data class MistralDeltaToolCall(
    val index: Int? = null,
    val id: String? = null,
    val function: MistralDeltaFunction? = null,
)

@Serializable
data class MistralDeltaFunction(
    val name: String? = null,
    val arguments: String? = null,
)

@Serializable
data class MistralUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
)

@Serializable
data class MistralModelListResponse(
    val data: List<MistralModel>,
)

@Serializable
data class MistralModel(
    val id: String,
    @SerialName("owned_by") val ownedBy: String? = null,
    val capabilities: MistralModelCapabilities? = null,
)

@Serializable
data class MistralModelCapabilities(
    val completionChat: Boolean = false,
    val completionFim: Boolean = false,
    val functionCalling: Boolean = false,
    val fineTuning: Boolean = false,
)
