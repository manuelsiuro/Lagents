package com.msa.lagents.domain.local

import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class LocalModelDescriptor(
    val id: String,
    val path: String,
    val engine: LocalModelEngineType,
    val displayName: String,
    val sizeBytes: Long,
    val contextWindowTokens: Int?,
    val status: LocalModelStatus = LocalModelStatus.NotLoaded
)

enum class LocalModelEngineType {
    MediaPipe,
    LlamaCpp
}

enum class LocalModelStatus {
    NotLoaded,
    Loading,
    Ready,
    Error
}

interface LocalModelEngine {
    val type: LocalModelEngineType
    
    suspend fun load(model: LocalModelDescriptor): Result<Unit>
    
    suspend fun unload()
    
    fun generate(request: GenerationRequest): Flow<GenerationEvent>
    
    fun isLoaded(): Boolean
}
