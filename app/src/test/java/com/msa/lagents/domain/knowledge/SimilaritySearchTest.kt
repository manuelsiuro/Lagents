package com.msa.lagents.domain.knowledge

import org.junit.Assert.assertEquals
import org.junit.Test

class SimilaritySearchTest {

    @Test
    fun `cosine similarity identical vectors`() {
        val v1 = listOf(1.0f, 0.0f, 0.5f)
        val v2 = listOf(1.0f, 0.0f, 0.5f)
        val result = SimilaritySearch.cosineSimilarity(v1, v2)
        assertEquals(1.0f, result, 0.001f)
    }

    @Test
    fun `cosine similarity orthogonal vectors`() {
        val v1 = listOf(1.0f, 0.0f)
        val v2 = listOf(0.0f, 1.0f)
        val result = SimilaritySearch.cosineSimilarity(v1, v2)
        assertEquals(0.0f, result, 0.001f)
    }

    @Test
    fun `cosine similarity opposite vectors`() {
        val v1 = listOf(1.0f, 1.0f)
        val v2 = listOf(-1.0f, -1.0f)
        val result = SimilaritySearch.cosineSimilarity(v1, v2)
        assertEquals(-1.0f, result, 0.001f)
    }
}
