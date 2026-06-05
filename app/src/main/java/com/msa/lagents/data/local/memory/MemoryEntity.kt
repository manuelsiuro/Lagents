package com.msa.lagents.data.local.memory

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memories",
    indices = [
        Index(value = ["reviewStatus"]),
        Index(value = ["archivedAtMillis"]),
    ],
)
data class MemoryEntity(
    @PrimaryKey val id: String,
    val content: String,
    val sourceType: String,
    val sourceId: String?,
    val reviewStatus: String,
    val tagsJson: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val archivedAtMillis: Long?,
)
