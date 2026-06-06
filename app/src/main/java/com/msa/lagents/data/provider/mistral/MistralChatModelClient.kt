package com.msa.lagents.data.provider.mistral

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
import java.io.IOException

class MistralChatModelClient(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.mistral.ai/v1",
) : ChatModelClient {
    override val providerId: String = "mistral"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getModels(): List<ModelDescriptor> {
        val request = Request.Builder()
            .url("$baseUrl/models")
            .header("Authorization", "Bearer $apiKey")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to list Mistral models: ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val modelList = json.decodeFromString<MistralModelListResponse>(body)
            return modelList.data.map { model ->
                ModelDescriptor(
                    id = model.id,
                    providerId = providerId,
                    displayName = model.id,
                    isLocal = false,
                    capabilities = buildList {
                        add(ModelCapability.Chat)
                        if (model.capabilities?.functionCalling == true) add(ModelCapability.Tools)
                    },
                    contextWindowTokens = null,
                    costPer1kInputTokensCents = null,
                    costPer1kOutputTokensCents = null,
                )
            }
        }
    }

    override fun generate(request: GenerationRequest): Flow<GenerationEvent> = callbackFlow {
        val mistralRequest = MistralChatRequest(
            model = request.modelId,
            messages = request.messages.map { it.toMistral() },
            stream = true,
            tools = request.tools.takeIf { it.isNotEmpty() }?.map {
                MistralTool(
                    function = MistralFunctionDefinition(
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
            .post(json.encodeToString(MistralChatRequest.serializer(), mistralRequest).toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(httpClient).newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    trySend(GenerationEvent.Finished).getOrThrow()
                    close()
                    return
                }

                try {
                    val chunk = json.decodeFromString<MistralChatChunk>(data)
                    chunk.choices.firstOrNull()?.let { choice ->
                        choice.delta.content?.let {
                            trySend(GenerationEvent.TextDelta(it)).getOrThrow()
                        }
                        choice.delta.toolCalls?.forEach { toolCall ->
                            trySend(
                                GenerationEvent.ToolCallDelta(
                                    index = toolCall.index ?: 0,
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
                    // Skip malformed
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val error = if (response != null) {
                    ProviderError.fromHttpCode(response.code, response.message.ifBlank { t?.message ?: "Unknown Mistral error" })
                } else {
                    ProviderError.Network(t?.message ?: "Network error connecting to Mistral")
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

    private fun ChatMessage.toMistral() = MistralMessage(
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
