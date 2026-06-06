package com.msa.lagents.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.prompt.PromptVersionEntity
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.skill.SkillVersionEntity
import com.msa.lagents.data.local.tool.ToolConfigEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomDaoTest {

    private lateinit var db: LagentsDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LagentsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun agentDao_upsert_and_observe() = runTest {
        val agent = AgentEntity(
            id = "agent-1",
            name = "Test Agent",
            description = "Test Description",
            systemBehavior = "Be helpful",
            defaultProviderId = null,
            defaultModelId = null,
            enabledSkillIdsJson = "[]",
            enabledToolIdsJson = "[]",
            promptDefaultsJson = "{}",
            memoryPolicyJson = "{}",
            workflowPermissionsJson = "{}",
            voiceProfileId = null,
            createdAtMillis = 1000L,
            updatedAtMillis = 1000L,
            archivedAtMillis = null,
        )
        db.agentDao().upsertAgent(agent)

        val agents = db.agentDao().observeActiveAgents().first()
        assertEquals(1, agents.size)
        assertEquals(agent, agents[0])

        val observedAgent = db.agentDao().observeAgent("agent-1").first()
        assertEquals(agent, observedAgent)
    }

    @Test
    fun agentDao_archive_and_restore() = runTest {
        val agent = AgentEntity(
            id = "agent-1",
            name = "Test Agent",
            description = "Test Description",
            systemBehavior = "Be helpful",
            defaultProviderId = null,
            defaultModelId = null,
            enabledSkillIdsJson = "[]",
            enabledToolIdsJson = "[]",
            promptDefaultsJson = "{}",
            memoryPolicyJson = "{}",
            workflowPermissionsJson = "{}",
            voiceProfileId = null,
            createdAtMillis = 1000L,
            updatedAtMillis = 1000L,
            archivedAtMillis = null,
        )
        db.agentDao().upsertAgent(agent)

        db.agentDao().archiveAgent("agent-1", 2000L)
        val activeAgents = db.agentDao().observeActiveAgents().first()
        assertTrue(activeAgents.isEmpty())

        db.agentDao().restoreAgent("agent-1", 3000L)
        val restoredAgents = db.agentDao().observeActiveAgents().first()
        assertEquals(1, restoredAgents.size)
        assertNull(restoredAgents[0].archivedAtMillis)
        assertEquals(3000L, restoredAgents[0].updatedAtMillis)
    }

    @Test
    fun promptDao_upsert_with_version() = runTest {
        val prompt = PromptEntity(
            id = "prompt-1",
            title = "Test Prompt",
            description = "Test Description",
            tagsJson = "[]",
            variableNamesJson = "[]",
            systemText = "System",
            userText = "User",
            defaultProviderId = null,
            defaultModelId = null,
            currentVersion = 1,
            createdAtMillis = 1000L,
            updatedAtMillis = 1000L,
            archivedAtMillis = null,
        )
        val version = PromptVersionEntity(
            promptId = "prompt-1",
            version = 1,
            title = prompt.title,
            description = prompt.description,
            tagsJson = prompt.tagsJson,
            variableNamesJson = prompt.variableNamesJson,
            systemText = prompt.systemText,
            userText = prompt.userText,
            createdAtMillis = 1000L,
        )
        db.promptDao().upsertPromptWithVersion(prompt, version)

        val prompts = db.promptDao().observeActivePrompts().first()
        assertEquals(1, prompts.size)

        val versions = db.promptDao().observePromptVersions("prompt-1").first()
        assertEquals(1, versions.size)
        assertEquals(version, versions[0])
    }

    @Test
    fun skillDao_upsert_with_version() = runTest {
        val skill = SkillEntity(
            id = "skill-1",
            title = "Test Skill",
            description = "Test Description",
            instructions = "Instructions",
            requiredPromptIdsJson = "[]",
            enabledToolIdsJson = "[]",
            requiredInputsJson = "[]",
            outputFormatJson = "{}",
            modelRequirementsJson = "{}",
            memoryPolicyJson = "{}",
            safetyPolicyJson = "{}",
            currentVersion = 1,
            createdAtMillis = 1000L,
            updatedAtMillis = 1000L,
            archivedAtMillis = null,
        )
        val version = SkillVersionEntity(
            skillId = "skill-1",
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
            createdAtMillis = 1000L,
        )
        db.skillDao().upsertSkillWithVersion(skill, version)

        val skills = db.skillDao().observeActiveSkills().first()
        assertEquals(1, skills.size)

        val versions = db.skillDao().observeSkillVersions("skill-1").first()
        assertEquals(1, versions.size)
        assertEquals(version, versions[0])
    }

    @Test
    fun toolConfigDao_upsert_and_observe() = runTest {
        val tool = ToolConfigEntity(
            id = "tool-1",
            toolKey = "test.tool",
            displayName = "Test Tool",
            description = "Test Description",
            category = "Test",
            enabled = true,
            requiresApproval = true,
            permissionPolicyJson = "{}",
            configJson = "{}",
            createdAtMillis = 1000L,
            updatedAtMillis = 1000L,
        )
        db.toolConfigDao().upsertToolConfig(tool)

        val tools = db.toolConfigDao().observeToolConfigs().first()
        assertEquals(1, tools.size)
        assertEquals(tool, tools[0])

        db.toolConfigDao().setToolEnabled("test.tool", false, 2000L)
        val updatedTool = db.toolConfigDao().observeToolConfig("test.tool").first()
        assertNotNull(updatedTool)
        assertEquals(false, updatedTool?.enabled)
        assertEquals(2000L, updatedTool?.updatedAtMillis)
    }
}
