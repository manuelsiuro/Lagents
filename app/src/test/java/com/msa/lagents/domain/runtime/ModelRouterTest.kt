package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.provider.ProviderRepository
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.PrivacyMode
import com.msa.lagents.domain.model.ModelDescriptor
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ModelRouterTest {

    private val providerRepository: ProviderRepository = mock()
    private lateinit var router: ModelRouter

    @Before
    fun setUp() {
        router = ModelRouter(providerRepository)
    }

    @Test
    fun `route respects local-only mode`() = runTest {
        val models = listOf(
            createTestModel("cloud-1", "openai", isLocal = false),
            createTestModel("local-1", "local", isLocal = true)
        )
        whenever(providerRepository.allModels).thenReturn(flowOf(models))

        val settings = AppSettings(privacyMode = PrivacyMode.LocalOnly)
        val agent = createTestAgent()

        val result = router.route(agent, settings)
        assertEquals("local-1", result?.id)
    }

    @Test
    fun `route respects agent override`() = runTest {
        val models = listOf(
            createTestModel("gpt-4", "openai", isLocal = false),
            createTestModel("claude-3", "anthropic", isLocal = false)
        )
        whenever(providerRepository.allModels).thenReturn(flowOf(models))

        val settings = AppSettings(privacyMode = PrivacyMode.Standard)
        val agent = createTestAgent(providerId = "anthropic", modelId = "claude-3")

        val result = router.route(agent, settings)
        assertEquals("claude-3", result?.id)
    }

    private fun createTestModel(id: String, providerId: String, isLocal: Boolean) = ModelDescriptor(
        id = id, providerId = providerId, displayName = id, isLocal = isLocal,
        capabilities = emptyList(), contextWindowTokens = null,
        costPer1kInputTokensCents = null, costPer1kOutputTokensCents = null
    )

    private fun createTestAgent(providerId: String? = null, modelId: String? = null) = AgentEntity(
        id = "agent-1", name = "", description = "", systemBehavior = "",
        defaultProviderId = providerId, defaultModelId = modelId,
        enabledSkillIdsJson = "[]", enabledToolIdsJson = "[]",
        promptDefaultsJson = "{}", memoryPolicyJson = "{}", workflowPermissionsJson = "{}",
        voiceProfileId = null, createdAtMillis = 0, updatedAtMillis = 0, archivedAtMillis = null
    )
}
