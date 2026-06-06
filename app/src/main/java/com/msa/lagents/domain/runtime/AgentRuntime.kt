package com.msa.lagents.domain.runtime

import com.msa.lagents.domain.model.GenerationEvent
import kotlinx.coroutines.flow.Flow

interface AgentRuntime {
    fun run(
        agentId: String,
        conversationId: String,
        userInput: String,
        variables: Map<String, String> = emptyMap(),
    ): Flow<GenerationEvent>

    suspend fun provideApproval(approved: Boolean)
}
