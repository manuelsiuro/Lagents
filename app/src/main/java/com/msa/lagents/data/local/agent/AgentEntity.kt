package com.msa.lagents.data.local.agent

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agents",
    indices = [
        Index(value = ["name"]),
        Index(value = ["archivedAtMillis"]),
    ],
)
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val systemBehavior: String,
    val defaultProviderId: String?,
    val defaultModelId: String?,
    val enabledSkillIdsJson: String,
    val enabledToolIdsJson: String,
    val promptDefaultsJson: String,
    val memoryPolicyJson: String,
    val workflowPermissionsJson: String,
    val voiceProfileId: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val archivedAtMillis: Long?,
)
