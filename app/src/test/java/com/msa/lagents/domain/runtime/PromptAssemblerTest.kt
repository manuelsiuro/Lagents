package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptAssemblerTest {

    private val assembler = PromptAssembler()

    private val baseAgent = AgentEntity(
        id = "agent-1",
        name = "Test Agent",
        description = "",
        systemBehavior = "You are a helpful assistant.",
        defaultProviderId = null,
        defaultModelId = null,
        enabledSkillIdsJson = "[]",
        enabledToolIdsJson = "[]",
        promptDefaultsJson = "{}",
        memoryPolicyJson = "{}",
        workflowPermissionsJson = "{}",
        voiceProfileId = null,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
        archivedAtMillis = null
    )

    @Test
    fun `assembleSystemPrompt with only agent`() {
        val result = assembler.assembleSystemPrompt(baseAgent)
        assertEquals("You are a helpful assistant.", result)
    }

    @Test
    fun `assembleSystemPrompt with skill`() {
        val skill = SkillEntity(
            id = "skill-1",
            title = "Test Skill",
            description = "",
            instructions = "Follow these steps.",
            requiredPromptIdsJson = "[]",
            enabledToolIdsJson = "[]",
            requiredInputsJson = "[]",
            outputFormatJson = "{}",
            modelRequirementsJson = "{}",
            memoryPolicyJson = "{}",
            safetyPolicyJson = "{}",
            currentVersion = 1,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
            archivedAtMillis = null
        )
        val result = assembler.assembleSystemPrompt(baseAgent, skill = skill)
        assertEquals("You are a helpful assistant.\n\nSkill Instructions:\nFollow these steps.", result)
    }

    @Test
    fun `assembleSystemPrompt with variables in prompt system text`() {
        val prompt = PromptEntity(
            id = "prompt-1",
            title = "Test Prompt",
            description = "",
            tagsJson = "[]",
            variableNamesJson = "[]",
            systemText = "Focus on {{topic}}.",
            userText = "",
            defaultProviderId = null,
            defaultModelId = null,
            currentVersion = 1,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
            archivedAtMillis = null
        )
        val result = assembler.assembleSystemPrompt(baseAgent, prompt = prompt, variables = mapOf("topic" to "AI"))
        assertEquals("You are a helpful assistant.\n\nAdditional Context:\nFocus on AI.", result)
    }

    @Test
    fun `assembleUserText replaces variables`() {
        val prompt = PromptEntity(
            id = "prompt-1",
            title = "Test Prompt",
            description = "",
            tagsJson = "[]",
            variableNamesJson = "[]",
            systemText = "",
            userText = "Tell me about {{item}}.",
            defaultProviderId = null,
            defaultModelId = null,
            currentVersion = 1,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
            archivedAtMillis = null
        )
        val result = assembler.assembleUserText(prompt, mapOf("item" to "Robots"))
        assertEquals("Tell me about Robots.", result)
    }
}
