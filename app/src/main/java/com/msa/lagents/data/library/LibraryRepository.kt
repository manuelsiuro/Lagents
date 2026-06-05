package com.msa.lagents.data.library

import com.msa.lagents.data.local.agent.AgentDao
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptDao
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.prompt.PromptVersionEntity
import com.msa.lagents.data.local.skill.SkillDao
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.skill.SkillVersionEntity
import com.msa.lagents.data.local.tool.ToolConfigDao
import com.msa.lagents.data.local.tool.ToolConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class LibraryRepository(
    private val agentDao: AgentDao,
    private val promptDao: PromptDao,
    private val skillDao: SkillDao,
    private val toolConfigDao: ToolConfigDao,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val overview: Flow<LibraryOverview> = combine(
        agentDao.observeActiveAgents(),
        promptDao.observeActivePrompts(),
        skillDao.observeActiveSkills(),
        toolConfigDao.observeToolConfigs(),
    ) { agents, prompts, skills, tools ->
        LibraryOverview(
            agents = agents,
            prompts = prompts,
            skills = skills,
            tools = tools,
        )
    }

    suspend fun createStarterAgent() {
        val now = nowMillis()
        val id = idGenerator()
        agentDao.upsertAgent(
            AgentEntity(
                id = id,
                name = "New agent",
                description = "Draft agent profile",
                systemBehavior = "You are a helpful AI agent. Ask before using side-effectful tools.",
                defaultProviderId = null,
                defaultModelId = null,
                enabledSkillIdsJson = "[]",
                enabledToolIdsJson = "[]",
                promptDefaultsJson = "{}",
                memoryPolicyJson = """{"mode":"user_approved"}""",
                workflowPermissionsJson = """{"sideEffects":"confirm_each"}""",
                voiceProfileId = null,
                createdAtMillis = now,
                updatedAtMillis = now,
                archivedAtMillis = null,
            ),
        )
    }

    suspend fun createStarterPrompt() {
        val now = nowMillis()
        val id = idGenerator()
        val prompt = PromptEntity(
            id = id,
            title = "New prompt",
            description = "Reusable prompt template",
            tagsJson = "[]",
            variableNamesJson = "[]",
            systemText = "You are concise, accurate, and explicit about assumptions.",
            userText = "{{input}}",
            defaultProviderId = null,
            defaultModelId = null,
            currentVersion = 1,
            createdAtMillis = now,
            updatedAtMillis = now,
            archivedAtMillis = null,
        )
        promptDao.upsertPromptWithVersion(
            prompt = prompt,
            version = PromptVersionEntity(
                promptId = id,
                version = 1,
                title = prompt.title,
                description = prompt.description,
                tagsJson = prompt.tagsJson,
                variableNamesJson = prompt.variableNamesJson,
                systemText = prompt.systemText,
                userText = prompt.userText,
                createdAtMillis = now,
            ),
        )
    }

    suspend fun createStarterSkill() {
        val now = nowMillis()
        val id = idGenerator()
        val skill = SkillEntity(
            id = id,
            title = "New skill",
            description = "Prompt-plus-tools bundle",
            instructions = "Use the selected prompt and approved tools to complete the user request.",
            requiredPromptIdsJson = "[]",
            enabledToolIdsJson = "[]",
            requiredInputsJson = "[]",
            outputFormatJson = """{"type":"markdown"}""",
            modelRequirementsJson = "{}",
            memoryPolicyJson = """{"mode":"agent_default"}""",
            safetyPolicyJson = """{"sideEffects":"confirm_each"}""",
            currentVersion = 1,
            createdAtMillis = now,
            updatedAtMillis = now,
            archivedAtMillis = null,
        )
        skillDao.upsertSkillWithVersion(
            skill = skill,
            version = SkillVersionEntity(
                skillId = id,
                version = 1,
                title = skill.title,
                description = skill.description,
                instructions = skill.instructions,
                requiredPromptIdsJson = skill.requiredPromptIdsJson,
                enabledToolIdsJson = skill.enabledToolIdsJson,
                requiredInputsJson = skill.requiredInputsJson,
                outputFormatJson = skill.outputFormatJson,
                modelRequirementsJson = skill.modelRequirementsJson,
                memoryPolicyJson = skill.memoryPolicyJson,
                safetyPolicyJson = skill.safetyPolicyJson,
                createdAtMillis = now,
            ),
        )
    }

    suspend fun createStarterToolConfig() {
        val now = nowMillis()
        val id = idGenerator()
        toolConfigDao.upsertToolConfig(
            ToolConfigEntity(
                id = id,
                toolKey = "custom.tool.${id.take(8)}",
                displayName = "New tool config",
                description = "Declarative wrapper around an approved app capability",
                category = "Custom",
                enabled = false,
                requiresApproval = true,
                permissionPolicyJson = """{"sideEffects":"confirm_each"}""",
                configJson = "{}",
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
    }
}

data class LibraryOverview(
    val agents: List<AgentEntity> = emptyList(),
    val prompts: List<PromptEntity> = emptyList(),
    val skills: List<SkillEntity> = emptyList(),
    val tools: List<ToolConfigEntity> = emptyList(),
)
