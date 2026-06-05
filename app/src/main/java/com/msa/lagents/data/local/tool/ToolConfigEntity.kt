package com.msa.lagents.data.local.tool

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tool_configs",
    indices = [
        Index(value = ["toolKey"], unique = true),
        Index(value = ["category"]),
    ],
)
data class ToolConfigEntity(
    @PrimaryKey val id: String,
    val toolKey: String,
    val displayName: String,
    val description: String,
    val category: String,
    val enabled: Boolean,
    val requiresApproval: Boolean,
    val permissionPolicyJson: String,
    val configJson: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
