package com.msa.lagents.data.local.skill

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skills",
    indices = [
        Index(value = ["title"]),
        Index(value = ["archivedAtMillis"]),
    ],
)
data class SkillEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val instructions: String,
    val requiredPromptIdsJson: String,
    val enabledToolIdsJson: String,
    val requiredInputsJson: String,
    val outputFormatJson: String,
    val modelRequirementsJson: String,
    val memoryPolicyJson: String,
    val safetyPolicyJson: String,
    val currentVersion: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val archivedAtMillis: Long?,
)

@Entity(
    tableName = "skill_versions",
    primaryKeys = ["skillId", "version"],
    indices = [
        Index(value = ["skillId"]),
    ],
)
data class SkillVersionEntity(
    val skillId: String,
    val version: Int,
    val title: String,
    val description: String,
    val instructions: String,
    val requiredPromptIdsJson: String,
    val enabledToolIdsJson: String,
    val requiredInputsJson: String,
    val outputFormatJson: String,
    val modelRequirementsJson: String,
    val memoryPolicyJson: String,
    val safetyPolicyJson: String,
    val createdAtMillis: Long,
)
