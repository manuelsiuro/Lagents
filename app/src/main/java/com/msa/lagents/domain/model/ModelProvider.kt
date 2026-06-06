package com.msa.lagents.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelDescriptor(
    val id: String,
    val providerId: String,
    val displayName: String,
    val isLocal: Boolean,
    val capabilities: List<ModelCapability>,
    val contextWindowTokens: Int?,
    val costPer1kInputTokensCents: Int?,
    val costPer1kOutputTokensCents: Int?,
)

enum class ModelCapability {
    Chat,
    Tools,
    Vision,
    AudioInput,
    AudioOutput,
}

@Serializable
data class GenerationRequest(
    val modelId: String,
    val messages: List<ChatMessage>,
    val systemPrompt: String? = null,
    val tools: List<ToolDefinition> = emptyList(),
    val temperature: Float? = null,
    val maxTokens: Int? = null,
)

@Serializable
data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val toolCallId: String? = null,
    val toolResultId: String? = null,
)

enum class MessageRole {
    System,
    User,
    Assistant,
    Tool,
}

@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val parametersJsonSchema: String,
)

@Serializable
sealed class GenerationEvent {
    @Serializable
    data class TextDelta(val delta: String) : GenerationEvent()

    @Serializable
    data class ToolCallDelta(
        val index: Int,
        val id: String?,
        val name: String?,
        val argumentsDelta: String?,
    ) : GenerationEvent()

    @Serializable
    data class Usage(
        val inputTokens: Int,
        val outputTokens: Int,
        val totalTokens: Int,
    ) : GenerationEvent()

    @Serializable
    data class ToolApprovalRequest(
        val callId: String,
        val toolName: String,
        val argumentsJson: String,
    ) : GenerationEvent()

    @Serializable
    data class Citation(
        val documentId: String,
        val title: String,
        val snippet: String,
        val pageNumber: Int? = null,
    ) : GenerationEvent()

    @Serializable
    data class MemorySuggestion(
        val id: String,
        val content: String,
    ) : GenerationEvent()

    @Serializable
    data class WorkflowProgress(
        val workflowId: String,
        val status: String,
        val progress: Float? = null,
    ) : GenerationEvent()

    @Serializable
    data class Error(val error: ProviderError) : GenerationEvent()

    @Serializable
    object Finished : GenerationEvent()
}
