package com.msa.lagents.data.local.workflow

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workflow_definitions",
    indices = [
        Index(value = ["agentId"]),
    ],
)
data class WorkflowDefinitionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val goal: String,
    val agentId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "workflow_runs",
    indices = [
        Index(value = ["workflowId"]),
    ],
)
data class WorkflowRunEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val status: String, // Pending, Running, Completed, Failed, Cancelled
    val progress: Float = 0f,
    val currentStep: String? = null,
    val logs: String = "", // JSON or formatted text
    val result: String? = null,
    val error: String? = null,
    val pendingApprovalCallId: String? = null,
    val pendingApprovalToolName: String? = null,
    val pendingApprovalArguments: String? = null,
    val approvalDecision: Boolean? = null, // null = waiting, true = approved, false = denied
    val startedAtMillis: Long,
    val finishedAtMillis: Long? = null,
)
