package com.msa.lagents.domain.knowledge

data class Chunk(
    val content: String,
    val startIndex: Int,
    val endIndex: Int
)

class TextChunker(
    private val maxChunkSize: Int = 1000,
    private val overlapSize: Int = 200
) {
    fun chunk(text: String): List<Chunk> {
        if (text.isBlank()) return emptyList()
        if (text.length <= maxChunkSize) return listOf(Chunk(text, 0, text.length))

        val chunks = mutableListOf<Chunk>()
        var start = 0
        
        while (start < text.length) {
            var end = start + maxChunkSize
            if (end > text.length) end = text.length
            
            // Try to find a good breaking point (newline or space) if not at the end
            if (end < text.length) {
                val lastNewline = text.lastIndexOf('\n', end)
                if (lastNewline > start + (maxChunkSize / 2)) {
                    end = lastNewline + 1
                } else {
                    val lastSpace = text.lastIndexOf(' ', end)
                    if (lastSpace > start + (maxChunkSize / 2)) {
                        end = lastSpace + 1
                    }
                }
            }
            
            chunks.add(Chunk(text.substring(start, end), start, end))
            
            if (end == text.length) break
            
            start = end - overlapSize
            if (start < 0) start = 0
            // Safety to avoid infinite loops if overlap is too large
            if (start >= end) start = end + 1 
        }
        
        return chunks
    }
}
