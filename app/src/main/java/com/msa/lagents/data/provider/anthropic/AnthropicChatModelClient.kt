package com.msa.lagents.data.provider.anthropic

import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import com.msa.lagents.domain.model.ModelCapability
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.model.ProviderError
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class AnthropicChatModelClient(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.anthropic.com/v1",
    private val apiVersion: String = "2023-06-01",
) : ChatModelClient {
    override val providerId: String = "anthropic"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getModels(): List<ModelDescriptor> {
        // Anthropic doesn't have a public /models endpoint. 
        // Returning a curated list of current models.
        return listOf(
            "claude-3-5-sonnet-20240620",
            "claude-3-opus-20240229",
            "claude-3-sonnet-20240229",
            "claude-3-haiku-20240307"
        ).map { id ->
            ModelDescriptor(
                id = id,
                providerId = providerId,
                displayName = id,
                isLocal = false,
                capabilities = listOf(ModelCapability.Chat, ModelCapability.Tools, ModelCapability.Vision),
                contextWindowTokens = 200_000,
                costPer1kInputTokensCents = null,
                costPer1kOutputTokensCents = null,
            )
        }
    }

    override fun generate(request: GenerationRequest): Flow<GenerationEvent> = callbackFlow {
        val anthropicRequest = AnthropicMessageRequest(
            model = request.modelId,
            messages = request.messages
                .filter { it.role != MessageRole.System }
                .map { it.toAnthropic() },
            system = request.systemPrompt,
            maxTokens = request.maxTokens ?: 4096,
            stream = true,
            tools = request.tools.takeIf { it.isNotEmpty() }?.map {
                AnthropicTool(
                    name = it.name,
                    description = it.description,
                    inputSchema = json.parseToJsonElement(it.parametersJsonSchema) as JsonObject
                )
            },
            temperature = request.temperature,
        )

        val httpRequest = Request.Builder()
            .url("$baseUrl/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", apiVersion)
            .post(json.encodeToString(AnthropicMessageRequest.serializer(), anthropicRequest).toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(httpClient).newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val event = json.decodeFromString<AnthropicStreamEvent>(data)
                    when (event) {
                        is AnthropicStreamEvent.ContentBlockDelta -> {
                            event.delta.text?.let {
                                trySend(GenerationEvent.TextDelta(it)).getOrThrow()
                            }
                            event.delta.partialJson?.let {
                                trySend(
                                    GenerationEvent.ToolCallDelta(
                                        index = event.index,
                                        id = null,
                                        name = null,
                                        argumentsDelta = it
                                    )
                                ).getOrThrow()
                            }
                        }
                        is AnthropicStreamEvent.ContentBlockStart -> {
                            val block = event.contentBlock
                            if (block is AnthropicContentBlock.ToolUse) {
                                trySend(
                                    GenerationEvent.ToolCallDelta(
                                        index = event.index,
                                        id = block.id,
                                        name = block.name,
                                        argumentsDelta = null
                                    )
                                ).getOrThrow()
                            }
                        }
                        is AnthropicStreamEvent.MessageDelta -> {
                            event.usage?.let {
                                trySend(
                                    GenerationEvent.Usage(
                                        inputTokens = it.inputTokens,
                                        outputTokens = it.outputTokens,
                                        totalTokens = it.inputTokens + it.outputTokens
                                    )
                                ).getOrThrow()
                            }
                        }
                        is AnthropicStreamEvent.MessageStart -> {
                            val usage = event.message.usage
                            trySend(
                                GenerationEvent.Usage(
                                    inputTokens = usage.inputTokens,
                                    outputTokens = usage.outputTokens,
                                    totalTokens = usage.inputTokens + usage.outputTokens
                                )
                            ).getOrThrow()
                        }
                        is AnthropicStreamEvent.MessageStop -> {
                            trySend(GenerationEvent.Finished).getOrThrow()
                            close()
                        }
                        is AnthropicStreamEvent.Error -> {
                            val error = when (event.error.type) {
                                "authentication_error" -> ProviderError.Authentication(event.error.message)
                                "rate_limit_error" -> ProviderError.RateLimit(event.error.message)
                                "overloaded_error" -> ProviderError.Unavailable(event.error.message)
                                else -> ProviderError.Unknown(event.error.message, event.error.type)
                            }
                            trySend(GenerationEvent.Error(error)).getOrThrow()
                            close()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore ping or unknown events
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val error = if (response != null) {
                    ProviderError.fromHttpCode(response.code, response.message.ifBlank { t?.message ?: "Unknown Anthropic error" })
                } else {
                    ProviderError.Network(t?.message ?: "Network error connecting to Anthropic")
                }
                trySend(GenerationEvent.Error(error)).getOrThrow()
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        })

        awaitClose {
            eventSource.cancel()
        }
    }

    private fun ChatMessage.toAnthropic() = AnthropicMessage(
        role = when (role) {
            MessageRole.User -> "user"
            MessageRole.Assistant -> "assistant"
            MessageRole.Tool -> "user"
            MessageRole.System -> "user"
        },
        content = when (role) {
            MessageRole.Tool -> listOf(
                AnthropicContentBlock.ToolResult(
                    toolUseId = toolResultId ?: "unknown",
                    content = content
                )
            )
            else -> listOf(AnthropicContentBlock.Text(text = content))
        }
    )
}
