package com.msa.lagents.domain.knowledge

interface EmbeddingEngine {
    suspend fun generateEmbedding(text: String): List<Float>
    suspend fun generateEmbeddings(texts: List<String>): List<List<Float>> = texts.map { generateEmbedding(it) }
}

class FakeEmbeddingEngine : EmbeddingEngine {
    override suspend fun generateEmbedding(text: String): List<Float> {
        // Deterministic pseudo-random embedding for testing
        val seed = text.hashCode().toLong()
        val random = java.util.Random(seed)
        return List(384) { random.nextFloat() }
    }
}
