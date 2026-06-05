package com.msa.lagents.data.local.prompt

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prompts",
    indices = [
        Index(value = ["title"]),
        Index(value = ["archivedAtMillis"]),
    ],
)
data class PromptEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val tagsJson: String,
    val variableNamesJson: String,
    val systemText: String,
    val userText: String,
    val defaultProviderId: String?,
    val defaultModelId: String?,
    val currentVersion: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val archivedAtMillis: Long?,
)

@Entity(
    tableName = "prompt_versions",
    primaryKeys = ["promptId", "version"],
    indices = [
        Index(value = ["promptId"]),
    ],
)
data class PromptVersionEntity(
    val promptId: String,
    val version: Int,
    val title: String,
    val description: String,
    val tagsJson: String,
    val variableNamesJson: String,
    val systemText: String,
    val userText: String,
    val createdAtMillis: Long,
)
