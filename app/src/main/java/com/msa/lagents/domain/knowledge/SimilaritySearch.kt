package com.msa.lagents.domain.knowledge

import com.msa.lagents.data.local.knowledge.KnowledgeChunkEntity
import kotlinx.serialization.json.Json
import kotlin.math.sqrt

object SimilaritySearch {
    fun cosineSimilarity(v1: List<Float>, v2: List<Float>): Float {
        if (v1.size != v2.size) return 0f
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        val denom = sqrt(norm1.toDouble()) * sqrt(norm2.toDouble())
        return if (denom == 0.0) 0f else (dotProduct / denom).toFloat()
    }

    fun findTopK(
        queryEmbedding: List<Float>,
        chunks: List<KnowledgeChunkEntity>,
        k: Int
    ): List<Pair<KnowledgeChunkEntity, Float>> {
        return chunks.mapNotNull { chunk ->
            chunk.embeddingJson?.let { json ->
                try {
                    val embedding = Json.decodeFromString<List<Float>>(json)
                    val score = cosineSimilarity(queryEmbedding, embedding)
                    chunk to score
                } catch (e: Exception) {
                    null
                }
            }
        }.sortedByDescending { it.second }
            .take(k)
    }
}
