package com.msa.lagents.data.local.knowledge

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_collections",
    indices = [
        Index(value = ["name"]),
    ],
)
data class KnowledgeCollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "knowledge_documents",
    indices = [
        Index(value = ["collectionId"]),
    ],
)
data class KnowledgeDocumentEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val title: String,
    val sourceUri: String,
    val status: String, // Pending, Indexed, Error
    val createdAtMillis: Long,
)

@Entity(
    tableName = "knowledge_chunks",
    indices = [
        Index(value = ["documentId"]),
    ],
)
data class KnowledgeChunkEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val content: String,
    val embeddingJson: String? = null,
    val indexInDocument: Int,
)
