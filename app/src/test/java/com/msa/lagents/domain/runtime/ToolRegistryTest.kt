package com.msa.lagents.domain.runtime

import com.msa.lagents.domain.model.ToolDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ToolRegistryTest {

    private val registry = ToolRegistry()

    @Test
    fun `register and get tool`() {
        val tool = Tool(
            definition = ToolDefinition(
                name = "test_tool",
                description = "A test tool",
                parametersJsonSchema = "{}"
            ),
            executor = object : ToolExecutor {
                override suspend fun execute(argumentsJson: String, context: ToolContext): String = "result"
            },
            category = "Test"
        )
        
        registry.register(tool)
        
        val retrieved = registry.getTool("test_tool")
        assertNotNull(retrieved)
        assertEquals("test_tool", retrieved?.definition?.name)
    }

    @Test
    fun `getAllTools returns all registered tools`() {
        registry.register(createTestTool("tool1"))
        registry.register(createTestTool("tool2"))
        
        val all = registry.getAllTools()
        assertEquals(2, all.size)
    }

    private fun createTestTool(name: String) = Tool(
        definition = ToolDefinition(name, "", "{}"),
        executor = object : ToolExecutor {
            override suspend fun execute(argumentsJson: String, context: ToolContext): String = ""
        },
        category = "Test"
    )
}
