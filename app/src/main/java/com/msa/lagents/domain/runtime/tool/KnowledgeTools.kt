package com.msa.lagents.domain.runtime.tool

import com.msa.lagents.data.knowledge.KnowledgeRepository
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.ToolDefinition
import com.msa.lagents.domain.runtime.Tool
import com.msa.lagents.domain.runtime.ToolContext
import com.msa.lagents.domain.runtime.ToolExecutor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SearchKnowledgeTool(
    private val repository: KnowledgeRepository,
) : ToolExecutor {

    @Serializable
    private data class Args(
        val collectionId: String,
        val query: String,
        val limit: Int = 3
    )

    override suspend fun execute(argumentsJson: String, context: ToolContext): String {
        val args = try {
            Json.decodeFromString<Args>(argumentsJson)
        } catch (e: Exception) {
            return "Error: Invalid arguments format. Expected JSON with collectionId and query."
        }

        val results = repository.search(args.collectionId, args.query, args.limit)
        
        if (results.isEmpty()) {
            return "No relevant information found in the collection."
        }

        val contextStrings = mutableListOf<String>()
        for (result in results) {
            context.emitEvent(
                GenerationEvent.Citation(
                    documentId = result.chunk.documentId,
                    title = "Source Document",
                    snippet = result.chunk.content
                )
            )
            contextStrings.add("Relevant snippet: ${result.chunk.content}")
        }

        return contextStrings.joinToString("\n\n")
    }

    companion object {
        fun create(repository: KnowledgeRepository): Tool {
            return Tool(
                definition = ToolDefinition(
                    name = "search_knowledge",
                    description = "Search a private knowledge collection for relevant information based on a query.",
                    parametersJsonSchema = """
                        {
                          "type": "object",
                          "properties": {
                            "collectionId": {
                              "type": "string",
                              "description": "The unique ID of the knowledge collection to search."
                            },
                            "query": {
                              "type": "string",
                              "description": "The search query or question to find relevant document chunks for."
                            },
                            "limit": {
                              "type": "integer",
                              "description": "Maximum number of results to return (default: 3)."
                            }
                          },
                          "required": ["collectionId", "query"]
                        }
                    """.trimIndent()
                ),
                executor = SearchKnowledgeTool(repository),
                category = "Knowledge"
            )
        }
    }
}
