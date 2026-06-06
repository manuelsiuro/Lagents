package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentDao
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.conversation.ConversationDao
import com.msa.lagents.data.local.provider.ProviderConfigDao
import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.data.provider.ChatModelClientFactory
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.domain.local.LocalModelEngine
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.model.ToolDefinition
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DefaultAgentRuntimeTest {

    private val agentDao: AgentDao = mock()
    private val conversationDao: ConversationDao = mock()
    private val providerConfigDao: ProviderConfigDao = mock()
    private val clientFactory: ChatModelClientFactory = mock()
    private val appSettingsRepository: AppSettingsRepository = mock()
    private val skillActivator: SkillActivator = mock()
    private val modelRouter: ModelRouter = mock()
    private val promptAssembler: PromptAssembler = mock()
    private val localModelEngine: LocalModelEngine = mock()

    private lateinit var runtime: DefaultAgentRuntime

    @Before
    fun setUp() {
        runtime = DefaultAgentRuntime(
            agentDao, conversationDao, providerConfigDao, clientFactory,
            appSettingsRepository, skillActivator, modelRouter, promptAssembler,
            localModelEngine,
            idGenerator = { "new-msg-id" },
            nowMillis = { 1000L }
        )
    }

    @Test
    fun `run completes text generation`() = runTest {
        val agent = createTestAgent()
        val providerConfig = createTestProviderConfig()
        val model = createTestModel()
        val client: ChatModelClient = mock()

        whenever(agentDao.observeAgent("agent-1")).thenReturn(flowOf(agent))
        whenever(skillActivator.resolveAgentAssets(agent)).thenReturn(SkillActivator.ActivationResult())
        whenever(appSettingsRepository.settings).thenReturn(flowOf(AppSettings()))
        whenever(modelRouter.route(any(), any())).thenReturn(model)
        whenever(providerConfigDao.observeProviderConfig("openai")).thenReturn(flowOf(providerConfig))
        whenever(clientFactory.create(providerConfig)).thenReturn(client)
        whenever(conversationDao.observeMessages("conv-1")).thenReturn(flowOf(emptyList()))
        whenever(promptAssembler.assembleSystemPrompt(any(), any(), any(), any())).thenReturn("System")

        whenever(client.generate(any())).thenReturn(flowOf(
            GenerationEvent.TextDelta("Hello"),
            GenerationEvent.Finished
        ))

        val events = runtime.run("agent-1", "conv-1", "Hi").toList()

        assertEquals(2, events.size)
        assertEquals(GenerationEvent.TextDelta("Hello"), events[0])
        assertEquals(GenerationEvent.Finished, events[1])
        
        org.mockito.kotlin.verify(conversationDao, org.mockito.kotlin.times(2)).upsertMessage(any())
    }

    @Test
    fun `run handles tool approval`() = runTest {
        val agent = createTestAgent()
        val providerConfig = createTestProviderConfig()
        val model = createTestModel()
        val client: ChatModelClient = mock()
        val tool: Tool = mock()
        val executor: ToolExecutor = mock()
        
        whenever(tool.definition).thenReturn(ToolDefinition("get_weather", "", "{}"))
        whenever(tool.requiresApproval).thenReturn(true)
        whenever(tool.executor).thenReturn(executor)
        whenever(executor.execute(any(), any())).thenReturn("Sunny")

        whenever(agentDao.observeAgent("agent-1")).thenReturn(flowOf(agent))
        whenever(skillActivator.resolveAgentAssets(agent)).thenReturn(
            SkillActivator.ActivationResult(tools = listOf(tool))
        )
        whenever(appSettingsRepository.settings).thenReturn(flowOf(AppSettings()))
        whenever(modelRouter.route(any(), any())).thenReturn(model)
        whenever(providerConfigDao.observeProviderConfig("openai")).thenReturn(flowOf(providerConfig))
        whenever(clientFactory.create(providerConfig)).thenReturn(client)
        whenever(conversationDao.observeMessages("conv-1")).thenReturn(flowOf(emptyList()))
        whenever(promptAssembler.assembleSystemPrompt(any(), any(), any(), any())).thenReturn("System")

        whenever(client.generate(any())).thenReturn(
            flowOf(
                GenerationEvent.ToolCallDelta(0, "call_1", "get_weather", "{}"),
                GenerationEvent.Finished
            ),
            flowOf(
                GenerationEvent.TextDelta("It is sunny"),
                GenerationEvent.Finished
            )
        )

        val eventsList = mutableListOf<GenerationEvent>()
        val job = launch {
            runtime.run("agent-1", "conv-1", "Hi").collect { 
                eventsList.add(it) 
            }
        }
        
        runCurrent()
        
        val approvalEvent = eventsList.filterIsInstance<GenerationEvent.ToolApprovalRequest>().firstOrNull()
        assertNotNull(approvalEvent)
        
        runtime.provideApproval(true)
        runCurrent()
        
        assertTrue(eventsList.any { it is GenerationEvent.TextDelta })
        job.cancel()
    }

    private fun createTestAgent() = AgentEntity(
        id = "agent-1", name = "Test", description = "", systemBehavior = "Be helpful",
        defaultProviderId = null, defaultModelId = null,
        enabledSkillIdsJson = "[]", enabledToolIdsJson = "[]",
        promptDefaultsJson = "{}", memoryPolicyJson = "{}", workflowPermissionsJson = "{}",
        voiceProfileId = null, createdAtMillis = 0, updatedAtMillis = 0, archivedAtMillis = null
    )

    private fun createTestProviderConfig() = ProviderConfigEntity(
        id = "openai", providerType = "openai", displayName = "OpenAI", enabled = true,
        apiKeyAlias = "key", baseUrl = null, defaultModelId = null,
        modelOverridesJson = "{}", budgetPolicyJson = "{}", createdAtMillis = 0, updatedAtMillis = 0
    )

    private fun createTestModel() = ModelDescriptor(
        id = "gpt-4", providerId = "openai", displayName = "GPT-4", isLocal = false,
        capabilities = emptyList(), contextWindowTokens = null,
        costPer1kInputTokensCents = null, costPer1kOutputTokensCents = null
    )
}
