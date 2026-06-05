package com.msa.lagents.data.local.debug

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debug_traces",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["workflowRunId"]),
        Index(value = ["createdAtMillis"]),
    ],
)
data class DebugTraceEntity(
    @PrimaryKey val id: String,
    val conversationId: String?,
    val workflowRunId: String?,
    val agentId: String?,
    val modelProviderId: String?,
    val modelId: String?,
    val promptPreview: String,
    val routingReason: String,
    val memoriesJson: String,
    val toolCallsJson: String,
    val knowledgeJson: String,
    val voiceJson: String,
    val usageJson: String,
    val errorJson: String?,
    val createdAtMillis: Long,
)
