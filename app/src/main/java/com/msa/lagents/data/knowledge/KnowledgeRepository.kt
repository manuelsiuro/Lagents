package com.msa.lagents.data.knowledge

import com.msa.lagents.data.local.knowledge.KnowledgeChunkEntity
import com.msa.lagents.data.local.knowledge.KnowledgeCollectionEntity
import com.msa.lagents.data.local.knowledge.KnowledgeDao
import com.msa.lagents.data.local.knowledge.KnowledgeDocumentEntity
import com.msa.lagents.domain.knowledge.EmbeddingEngine
import com.msa.lagents.domain.knowledge.PlainTextExtractor
import com.msa.lagents.domain.knowledge.SimilaritySearch
import com.msa.lagents.domain.knowledge.TextChunker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.util.UUID

class KnowledgeRepository(
    private val knowledgeDao: KnowledgeDao,
    private val embeddingEngine: EmbeddingEngine,
    private val textChunker: TextChunker = TextChunker(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val collections: Flow<List<KnowledgeCollectionEntity>> = knowledgeDao.observeCollections()

    fun observeDocuments(collectionId: String): Flow<List<KnowledgeDocumentEntity>> {
        return knowledgeDao.observeDocuments(collectionId)
    }

    suspend fun createCollection(name: String, description: String) {
        val now = nowMillis()
        knowledgeDao.upsertCollection(
            KnowledgeCollectionEntity(
                id = idGenerator(),
                name = name,
                description = description,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun deleteCollection(id: String) {
        knowledgeDao.deleteCollection(id)
    }

    suspend fun importDocument(
        collectionId: String,
        title: String,
        uri: String,
        inputStream: InputStream
    ) = withContext(Dispatchers.IO) {
        val documentId = idGenerator()
        val now = nowMillis()

        // 1. Save Document record
        val document = KnowledgeDocumentEntity(
            id = documentId,
            collectionId = collectionId,
            title = title,
            sourceUri = uri,
            status = "Processing",
            createdAtMillis = now
        )
        knowledgeDao.upsertDocument(document)

        try {
            // 2. Extract Text (Only support plain text/markdown for now)
            val extractor = PlainTextExtractor()
            val text = extractor.extract(inputStream)

            // 3. Chunk Text
            val chunks = textChunker.chunk(text)

            // 4. Generate Embeddings and Save Chunks
            val chunkEntities = chunks.mapIndexed { index, chunk ->
                val embedding = embeddingEngine.generateEmbedding(chunk.content)
                KnowledgeChunkEntity(
                    id = idGenerator(),
                    documentId = documentId,
                    content = chunk.content,
                    embeddingJson = Json.encodeToString(embedding),
                    indexInDocument = index
                )
            }
            knowledgeDao.insertChunks(chunkEntities)

            // 5. Update Status
            knowledgeDao.upsertDocument(document.copy(status = "Indexed"))
        } catch (e: Exception) {
            knowledgeDao.upsertDocument(document.copy(status = "Error"))
            throw e
        }
    }

    suspend fun search(collectionId: String, query: String, limit: Int = 5): List<SearchResult> {
        val queryEmbedding = embeddingEngine.generateEmbedding(query)
        val allChunks = knowledgeDao.getChunksForCollection(collectionId)
        
        return allChunks.mapNotNull { chunk ->
            val chunkEmbeddingJson = chunk.embeddingJson ?: return@mapNotNull null
            val chunkEmbedding = Json.decodeFromString<List<Float>>(chunkEmbeddingJson)
            val score = SimilaritySearch.cosineSimilarity(queryEmbedding, chunkEmbedding)
            SearchResult(chunk, score)
        }.sortedByDescending { it.score }.take(limit)
    }

    suspend fun deleteDocument(id: String) {
        knowledgeDao.deleteDocumentWithChunks(id)
    }
}

data class SearchResult(
    val chunk: KnowledgeChunkEntity,
    val score: Float
)
