package com.msa.lagents.data.provider.gemini

import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import com.msa.lagents.domain.model.ModelCapability
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.model.ProviderError
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import java.io.IOException

class GeminiChatModelClient(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
) : ChatModelClient {
    override val providerId: String = "gemini"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getModels(): List<ModelDescriptor> {
        val request = Request.Builder()
            .url("$baseUrl/models?key=$apiKey")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to list Gemini models: ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val modelList = json.decodeFromString<GeminiModelListResponse>(body)
            return modelList.models
                .filter { it.supportedGenerationMethods.contains("generateContent") }
                .map { model ->
                    ModelDescriptor(
                        id = model.name.removePrefix("models/"),
                        providerId = providerId,
                        displayName = model.displayName,
                        isLocal = false,
                        capabilities = listOf(ModelCapability.Chat, ModelCapability.Tools, ModelCapability.Vision),
                        contextWindowTokens = model.inputTokenLimit,
                        costPer1kInputTokensCents = null,
                        costPer1kOutputTokensCents = null,
                    )
                }
        }
    }

    override fun generate(request: GenerationRequest): Flow<GenerationEvent> = flow {
        val geminiRequest = GeminiGenerateRequest(
            contents = request.messages
                .filter { it.role != MessageRole.System }
                .map { it.toGemini() },
            systemInstruction = request.systemPrompt?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            },
            tools = request.tools.takeIf { it.isNotEmpty() }?.let {
                listOf(GeminiTool(functionDeclarations = it.map { tool ->
                    GeminiFunctionDeclaration(
                        name = tool.name,
                        description = tool.description,
                        parameters = json.parseToJsonElement(tool.parametersJsonSchema) as JsonObject
                    )
                }))
            },
            generationConfig = GeminiGenerationConfig(
                temperature = request.temperature,
                maxOutputTokens = request.maxTokens
            )
        )

        val modelId = if (request.modelId.startsWith("models/")) request.modelId else "models/${request.modelId}"
        val httpRequest = Request.Builder()
            .url("$baseUrl/$modelId:streamGenerateContent?alt=sse&key=$apiKey") // Trying alt=sse just in case it works now
            .post(json.encodeToString(GeminiGenerateRequest.serializer(), geminiRequest).toRequestBody("application/json".toMediaType()))
            .build()

        // Wait, if I use OkHttp with streamGenerateContent without SSE, I need to parse the JSON array.
        // Actually, Google's documentation for REST says it returns a stream of JSON objects if not using SSE.
        // But many users report it's a JSON array.
        
        // I'll try to implement a robust enough parser for the JSON array stream.
        
        httpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val error = ProviderError.fromHttpCode(response.code, response.message.ifBlank { "Unknown Gemini error" })
                emit(GenerationEvent.Error(error))
                return@flow
            }

            val source = response.body?.source() ?: return@flow
            parseGeminiStream(source).collect { event ->
                emit(event)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun parseGeminiStream(source: BufferedSource): Flow<GenerationEvent> = flow {
        var bracketCount = 0
        val buffer = StringBuilder()
        
        try {
            while (!source.exhausted()) {
                val char = source.readByte().toInt().toChar()
                
                if (char == '[') {
                    if (bracketCount == 0) {
                        bracketCount++
                        continue
                    }
                }
                
                if (char == '{') {
                    bracketCount++
                }
                
                if (bracketCount > 1) {
                    buffer.append(char)
                }
                
                if (char == '}') {
                    bracketCount--
                    if (bracketCount == 1) {
                        // End of a JSON object in the array
                        val jsonStr = buffer.toString()
                        buffer.setLength(0)
                        
                        try {
                            val resp = json.decodeFromString<GeminiResponse>(jsonStr)
                            resp.candidates?.firstOrNull()?.let { candidate ->
                                candidate.content?.parts?.forEach { part ->
                                    part.text?.let { emit(GenerationEvent.TextDelta(it)) }
                                    part.functionCall?.let {
                                        emit(GenerationEvent.ToolCallDelta(
                                            index = candidate.index ?: 0,
                                            id = null,
                                            name = it.name,
                                            argumentsDelta = it.args?.toString()
                                        ))
                                    }
                                }
                            }
                            resp.usageMetadata?.let {
                                emit(GenerationEvent.Usage(
                                    inputTokens = it.promptTokenCount,
                                    outputTokens = it.candidatesTokenCount,
                                    totalTokens = it.totalTokenCount
                                ))
                            }
                        } catch (e: Exception) {
                            // Skip malformed
                        }
                    }
                }
            }
            emit(GenerationEvent.Finished)
        } catch (e: Exception) {
            emit(GenerationEvent.Error(ProviderError.Unknown(e.message ?: "Stream error")))
        }
    }

    private fun ChatMessage.toGemini() = GeminiContent(
        role = when (role) {
            MessageRole.User -> "user"
            MessageRole.Assistant -> "model"
            MessageRole.Tool -> "user" // Tool results are parts of a message with role user
            else -> "user"
        },
        parts = when (role) {
            MessageRole.Tool -> listOf(
                GeminiPart(
                    functionResponse = GeminiFunctionResponse(
                        name = toolResultId ?: "unknown",
                        response = json.parseToJsonElement(content) as JsonObject
                    )
                )
            )
            else -> listOf(GeminiPart(text = content))
        }
    )
}
