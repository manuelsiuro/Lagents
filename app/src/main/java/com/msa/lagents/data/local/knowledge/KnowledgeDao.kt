package com.msa.lagents.data.local.knowledge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_collections ORDER BY updatedAtMillis DESC")
    fun observeCollections(): Flow<List<KnowledgeCollectionEntity>>

    @Query("SELECT * FROM knowledge_documents WHERE collectionId = :collectionId")
    fun observeDocuments(collectionId: String): Flow<List<KnowledgeDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollection(collection: KnowledgeCollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: KnowledgeDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<KnowledgeChunkEntity>)

    @Query("DELETE FROM knowledge_chunks WHERE documentId = :documentId")
    suspend fun deleteChunks(documentId: String)

    @Query("DELETE FROM knowledge_documents WHERE id = :documentId")
    suspend fun deleteDocument(documentId: String)

    @Transaction
    suspend fun deleteDocumentWithChunks(documentId: String) {
        deleteChunks(documentId)
        deleteDocument(documentId)
    }

    @Query("DELETE FROM knowledge_collections WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: String)

    @Query("SELECT * FROM knowledge_chunks WHERE documentId IN (SELECT id FROM knowledge_documents WHERE collectionId \u003d :collectionId)")
    suspend fun getChunksForCollection(collectionId: String): List<KnowledgeChunkEntity>
}
