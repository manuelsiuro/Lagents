package com.msa.lagents.domain.provider

import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow

interface ChatModelClient {
    val providerId: String

    suspend fun getModels(): List<ModelDescriptor>

    fun generate(request: GenerationRequest): Flow<GenerationEvent>
}
