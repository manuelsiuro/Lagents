package com.msa.lagents.domain.runtime

import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.ToolDefinition

interface ToolExecutor {
    suspend fun execute(argumentsJson: String, context: ToolContext): String
}

data class ToolContext(
    val emitEvent: suspend (GenerationEvent) -> Unit
)

data class Tool(
    val definition: ToolDefinition,
    val executor: ToolExecutor,
    val category: String,
    val requiresApproval: Boolean = false,
    val isSideEffectful: Boolean = false,
)
