package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptDao
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillDao
import com.msa.lagents.data.local.skill.SkillEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class SkillActivator(
    private val skillDao: SkillDao,
    private val promptDao: PromptDao,
    private val toolRegistry: ToolRegistry,
) {
    private val json = Json { ignoreUnknownKeys = true }

    data class ActivationResult(
        val skill: SkillEntity? = null,
        val prompts: List<PromptEntity> = emptyList(),
        val tools: List<Tool> = emptyList(),
    )

    suspend fun resolveAgentAssets(agent: AgentEntity): ActivationResult {
        val skillIds = json.decodeFromString<List<String>>(agent.enabledSkillIdsJson)
        val toolIds = json.decodeFromString<List<String>>(agent.enabledToolIdsJson)

        // For now, we only activate the first enabled skill if any, 
        // or just the explicitly enabled tools.
        // In a more complex runtime, we might activate multiple skills.
        
        val activatedSkill = if (skillIds.isNotEmpty()) {
            skillDao.observeSkill(skillIds.first()).first()
        } else null

        val skillPrompts = activatedSkill?.let { skill ->
            val promptIds = json.decodeFromString<List<String>>(skill.requiredPromptIdsJson)
            promptIds.mapNotNull { promptDao.observePrompt(it).first() }
        } ?: emptyList()

        val skillToolIds = activatedSkill?.let { 
            json.decodeFromString<List<String>>(it.enabledToolIdsJson)
        } ?: emptyList()

        val allToolIds = (toolIds + skillToolIds).distinct()
        val activatedTools = allToolIds.mapNotNull { toolRegistry.getTool(it) }

        return ActivationResult(
            skill = activatedSkill,
            prompts = skillPrompts,
            tools = activatedTools
        )
    }
}
