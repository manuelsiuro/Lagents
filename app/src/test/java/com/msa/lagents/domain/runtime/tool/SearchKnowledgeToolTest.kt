package com.msa.lagents.domain.runtime.tool

import com.msa.lagents.data.knowledge.KnowledgeRepository
import com.msa.lagents.data.knowledge.SearchResult
import com.msa.lagents.data.local.knowledge.KnowledgeChunkEntity
import com.msa.lagents.domain.runtime.ToolContext
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchKnowledgeToolTest {

    private val repository: KnowledgeRepository = mock()
    private val tool = SearchKnowledgeTool(repository)

    @Test
    fun `execute returns formatted results and emits citations`() = runTest {
        val chunk = KnowledgeChunkEntity("chunk-1", "doc-1", "This is the content", null, 0)
        val searchResults = listOf(SearchResult(chunk, 0.9f))
        
        whenever(repository.search(any(), any(), any())).thenReturn(searchResults)

        val citations = mutableListOf<com.msa.lagents.domain.model.GenerationEvent>()
        val context = ToolContext(emitEvent = { citations.add(it) })
        
        val result = tool.execute("""{"collectionId": "col-1", "query": "test"}""", context)
        
        assertTrue(result.contains("Relevant snippet: This is the content"))
        assertEquals(1, citations.size)
        assertTrue(citations[0] is com.msa.lagents.domain.model.GenerationEvent.Citation)
    }

    @Test
    fun `execute returns error message on invalid json`() = runTest {
        val context = ToolContext(emitEvent = {})
        val result = tool.execute("invalid json", context)
        assertTrue(result.startsWith("Error:"))
    }
}
