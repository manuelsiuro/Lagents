package com.msa.lagents.domain.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextChunkerTest {

    @Test
    fun `chunk small text returns single chunk`() {
        val chunker = TextChunker(maxChunkSize = 100)
        val text = "Hello world"
        val result = chunker.chunk(text)
        
        assertEquals(1, result.size)
        assertEquals(text, result[0].content)
        assertEquals(0, result[0].startIndex)
        assertEquals(text.length, result[0].endIndex)
    }

    @Test
    fun `chunk large text with overlap`() {
        val chunker = TextChunker(maxChunkSize = 50, overlapSize = 10)
        val text = "This is a long string that should be split into multiple chunks with some overlap."
        val result = chunker.chunk(text)
        
        assertTrue(result.size > 1)
        // Check overlap manually or via logic
        val firstChunkEnd = result[0].endIndex
        val secondChunkStart = result[1].startIndex
        assertEquals(firstChunkEnd - 10, secondChunkStart)
    }

    @Test
    fun `chunk respects newlines if possible`() {
        val chunker = TextChunker(maxChunkSize = 20, overlapSize = 5)
        val text = "Line one\nLine two\nLine three"
        val result = chunker.chunk(text)
        
        // "Line one\n" is 9 chars. "Line two\n" is 9 chars. 
        // 9 + 9 = 18 < 20. 
        // Should break after "Line two\n"
        assertTrue(result[0].content.contains("Line two\n"))
    }
}
