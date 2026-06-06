package com.msa.lagents.data.provider.openai

import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import com.msa.lagents.domain.model.ModelCapability
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.model.ProviderError
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.JsonObject

class OpenAiChatModelClient(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1",
) : ChatModelClient {
    override val providerId: String = "openai"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getModels(): List<ModelDescriptor> {
        val request = Request.Builder()
            .url("$baseUrl/models")
            .header("Authorization", "Bearer $apiKey")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to list OpenAI models: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty body")
            val openAiModels = json.decodeFromString<OpenAiModelsResponse>(body)
            return openAiModels.data.map { model ->
                ModelDescriptor(
                    id = model.id,
                    providerId = providerId,
                    displayName = model.id,
                    isLocal = false,
                    capabilities = listOf(ModelCapability.Chat, ModelCapability.Tools),
                    contextWindowTokens = null,
                    costPer1kInputTokensCents = null,
                    costPer1kOutputTokensCents = null,
                )
            }
        }
    }

    override fun generate(request: GenerationRequest): Flow<GenerationEvent> = callbackFlow {
        val openAiRequest = OpenAiChatRequest(
            model = request.modelId,
            messages = buildList {
                request.systemPrompt?.let {
                    add(OpenAiMessage(role = "system", content = it))
                }
                addAll(request.messages.map { it.toOpenAi() })
            },
            stream = true,
            tools = request.tools.takeIf { it.isNotEmpty() }?.map {
                OpenAiTool(
                    function = OpenAiFunctionDefinition(
                        name = it.name,
                        description = it.description,
                        parameters = json.parseToJsonElement(it.parametersJsonSchema) as JsonObject
                    )
                )
            },
            temperature = request.temperature,
            maxTokens = request.maxTokens,
        )

        val httpRequest = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(json.encodeToString(OpenAiChatRequest.serializer(), openAiRequest).toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(httpClient).newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    trySend(GenerationEvent.Finished)
                    close()
                    return
                }

                try {
                    val chunk = json.decodeFromString<OpenAiChatChunk>(data)
                    chunk.choices.firstOrNull()?.let { choice ->
                        choice.delta.content?.let {
                            trySend(GenerationEvent.TextDelta(it)).getOrThrow()
                        }
                        choice.delta.toolCalls?.forEach { toolCall ->
                            trySend(
                                GenerationEvent.ToolCallDelta(
                                    index = toolCall.index,
                                    id = toolCall.id,
                                    name = toolCall.function?.name,
                                    argumentsDelta = toolCall.function?.arguments,
                                )
                            ).getOrThrow()
                        }
                    }
                    chunk.usage?.let {
                        trySend(
                            GenerationEvent.Usage(
                                inputTokens = it.promptTokens,
                                outputTokens = it.completionTokens,
                                totalTokens = it.totalTokens
                            )
                        ).getOrThrow()
                    }
                } catch (e: Exception) {
                    // Ignore malformed chunks or usage chunks we can't parse yet
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val error = if (response != null) {
                    ProviderError.fromHttpCode(response.code, response.message.ifBlank { t?.message ?: "Unknown OpenAI error" })
                } else {
                    ProviderError.Network(t?.message ?: "Network error connecting to OpenAI")
                }
                trySend(GenerationEvent.Error(error)).getOrThrow()
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                trySend(GenerationEvent.Finished).getOrThrow()
                close()
            }
        })

        awaitClose {
            eventSource.cancel()
        }
    }

    private fun ChatMessage.toOpenAi() = OpenAiMessage(
        role = when (role) {
            MessageRole.System -> "system"
            MessageRole.User -> "user"
            MessageRole.Assistant -> "assistant"
            MessageRole.Tool -> "tool"
        },
        content = content,
        toolCallId = toolResultId,
    )
}
