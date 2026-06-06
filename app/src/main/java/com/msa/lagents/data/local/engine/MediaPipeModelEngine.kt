package com.msa.lagents.data.local.engine

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.msa.lagents.domain.local.LocalModelDescriptor
import com.msa.lagents.domain.local.LocalModelEngine
import com.msa.lagents.domain.local.LocalModelEngineType
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.ProviderError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

class MediaPipeModelEngine(private val context: Context) : LocalModelEngine {
    override val type: LocalModelEngineType = LocalModelEngineType.MediaPipe
    
    private var llmInference: LlmInference? = null
    private var currentModel: LocalModelDescriptor? = null
    private var activeEmitter: ((GenerationEvent) -> Unit)? = null

    override suspend fun load(model: LocalModelDescriptor): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            unload()
            
            val modelFile = File(model.path)
            if (!modelFile.exists()) {
                return@withContext Result.failure(Exception("Model file not found at ${model.path}"))
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(model.path)
                .setMaxTokens(model.contextWindowTokens ?: 512)
                .setTemperature(0.7f)
                .setResultListener { result, done ->
                    activeEmitter?.invoke(GenerationEvent.TextDelta(result))
                    if (done) {
                        activeEmitter?.invoke(GenerationEvent.Finished)
                    }
                }
                .setErrorListener { error ->
                    activeEmitter?.invoke(GenerationEvent.Error(ProviderError.Unknown(error.message ?: "Inference error")))
                }
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            currentModel = model
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unload() = withContext(Dispatchers.IO) {
        llmInference?.close()
        llmInference = null
        currentModel = null
        activeEmitter = null
    }

    override fun generate(request: GenerationRequest): Flow<GenerationEvent> = callbackFlow {
        val inference = llmInference
        if (inference == null) {
            trySend(GenerationEvent.Error(ProviderError.Unavailable("Local model not loaded"))).getOrThrow()
            close()
            return@callbackFlow
        }

        activeEmitter = { event ->
            trySend(event)
            if (event is GenerationEvent.Finished || event is GenerationEvent.Error) {
                close()
            }
        }

        val fullPrompt = buildString {
            request.systemPrompt?.let { 
                append(it)
                append("\n\n")
            }
            request.messages.forEach { msg ->
                append("${msg.role.name}: ${msg.content}\n")
            }
            append("Assistant: ")
        }

        try {
            inference.generateResponseAsync(fullPrompt)
        } catch (e: Exception) {
            trySend(GenerationEvent.Error(ProviderError.Unknown(e.message ?: "Inference failed"))).getOrThrow()
            close(e)
        }

        awaitClose {
            activeEmitter = null
        }
    }

    override fun isLoaded(): Boolean = llmInference != null
}
