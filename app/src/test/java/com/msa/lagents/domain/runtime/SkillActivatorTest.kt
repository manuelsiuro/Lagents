package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptDao
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillDao
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.domain.model.ToolDefinition
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SkillActivatorTest {

    private val skillDao: SkillDao = mock()
    private val promptDao: PromptDao = mock()
    private val toolRegistry: ToolRegistry = mock()
    private lateinit var activator: SkillActivator

    @Before
    fun setUp() {
        activator = SkillActivator(skillDao, promptDao, toolRegistry)
    }

    @Test
    fun `resolveAgentAssets resolves skill prompts and tools`() = runTest {
        val agent = createTestAgent(
            enabledSkillIds = listOf("skill-1"),
            enabledToolIds = listOf("tool-1")
        )
        val skill = createTestSkill(
            id = "skill-1",
            requiredPromptIds = listOf("prompt-1"),
            enabledToolIds = listOf("tool-2")
        )
        val prompt = createTestPrompt(id = "prompt-1")
        val tool1 = createTestTool("tool-1")
        val tool2 = createTestTool("tool-2")

        whenever(skillDao.observeSkill("skill-1")).thenReturn(flowOf(skill))
        whenever(promptDao.observePrompt("prompt-1")).thenReturn(flowOf(prompt))
        whenever(toolRegistry.getTool("tool-1")).thenReturn(tool1)
        whenever(toolRegistry.getTool("tool-2")).thenReturn(tool2)

        val result = activator.resolveAgentAssets(agent)

        assertEquals(skill, result.skill)
        assertEquals(1, result.prompts.size)
        assertEquals(prompt, result.prompts[0])
        assertEquals(2, result.tools.size)
        assertEquals(listOf(tool1, tool2), result.tools)
    }

    private fun createTestAgent(enabledSkillIds: List<String>, enabledToolIds: List<String>) = AgentEntity(
        id = "agent-1", name = "", description = "", systemBehavior = "",
        defaultProviderId = null, defaultModelId = null,
        enabledSkillIdsJson = "[${enabledSkillIds.joinToString(",") { "\"$it\"" }}]",
        enabledToolIdsJson = "[${enabledToolIds.joinToString(",") { "\"$it\"" }}]",
        promptDefaultsJson = "{}", memoryPolicyJson = "{}", workflowPermissionsJson = "{}",
        voiceProfileId = null, createdAtMillis = 0, updatedAtMillis = 0, archivedAtMillis = null
    )

    private fun createTestSkill(id: String, requiredPromptIds: List<String>, enabledToolIds: List<String>) = SkillEntity(
        id = id, title = "", description = "", instructions = "",
        requiredPromptIdsJson = "[${requiredPromptIds.joinToString(",") { "\"$it\"" }}]",
        enabledToolIdsJson = "[${enabledToolIds.joinToString(",") { "\"$it\"" }}]",
        requiredInputsJson = "[]", outputFormatJson = "{}", modelRequirementsJson = "{}",
        memoryPolicyJson = "{}", safetyPolicyJson = "{}", currentVersion = 1,
        createdAtMillis = 0, updatedAtMillis = 0, archivedAtMillis = null
    )

    private fun createTestPrompt(id: String) = PromptEntity(
        id = id, title = "", description = "", tagsJson = "[]", variableNamesJson = "[]",
        systemText = "", userText = "", defaultProviderId = null, defaultModelId = null,
        currentVersion = 1, createdAtMillis = 0, updatedAtMillis = 0, archivedAtMillis = null
    )

    private fun createTestTool(name: String) = Tool(
        definition = ToolDefinition(name, "", "{}"),
        executor = mock(),
        category = "Test"
    )
}
