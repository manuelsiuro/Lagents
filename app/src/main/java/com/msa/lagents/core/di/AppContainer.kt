package com.msa.lagents.core.di

import android.content.Context
import androidx.room.Room
import com.msa.lagents.data.knowledge.KnowledgeRepository
import com.msa.lagents.data.library.LibraryRepository
import com.msa.lagents.domain.knowledge.FakeEmbeddingEngine
import com.msa.lagents.domain.knowledge.TextChunker
import com.msa.lagents.data.local.LagentsDatabase
import com.msa.lagents.data.local.engine.MediaPipeModelEngine
import com.msa.lagents.data.local.manager.LocalModelManager
import com.msa.lagents.data.provider.ApiKeyStorage
import com.msa.lagents.data.provider.ChatModelClientFactory
import com.msa.lagents.data.provider.ProviderRepository
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.domain.runtime.AgentRuntime
import com.msa.lagents.domain.runtime.DefaultAgentRuntime
import com.msa.lagents.domain.runtime.ModelRouter
import com.msa.lagents.domain.runtime.PromptAssembler
import com.msa.lagents.domain.runtime.SkillActivator
import com.msa.lagents.domain.runtime.ToolRegistry
import com.msa.lagents.ui.chat.ChatViewModel
import com.msa.lagents.ui.debug.DebugViewModel
import com.msa.lagents.ui.knowledge.KnowledgeViewModel
import com.msa.lagents.ui.library.LibraryViewModel
import com.msa.lagents.ui.models.LocalModelManagerViewModel
import com.msa.lagents.ui.settings.AppSettingsViewModel
import com.msa.lagents.domain.runtime.tool.SearchKnowledgeTool
import com.msa.lagents.data.voice.AndroidSpeechToTextEngine
import com.msa.lagents.data.voice.AndroidTextToSpeechEngine
import com.msa.lagents.data.voice.VoiceRepository
import com.msa.lagents.data.workflow.WorkflowRepository
import com.msa.lagents.ui.workflows.WorkflowViewModel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val database: LagentsDatabase = Room.databaseBuilder(
        context = applicationContext,
        klass = LagentsDatabase::class.java,
        name = "lagents.db",
    ).build()

    val appSettingsRepository: AppSettingsRepository = AppSettingsRepository(
        context = applicationContext,
    )

    val apiKeyStorage: ApiKeyStorage = ApiKeyStorage(context = applicationContext)

    private val chatModelClientFactory: ChatModelClientFactory = ChatModelClientFactory(
        httpClient = httpClient,
        apiKeyStorage = apiKeyStorage,
    )

    val providerRepository: ProviderRepository = ProviderRepository(
        providerConfigDao = database.providerConfigDao(),
        clientFactory = chatModelClientFactory,
        apiKeyStorage = apiKeyStorage,
    )

    val localModelEngine: MediaPipeModelEngine = MediaPipeModelEngine(context = applicationContext)
    
    val localModelManager: LocalModelManager = LocalModelManager(engine = localModelEngine)

    val toolRegistry: ToolRegistry = ToolRegistry()

    val promptAssembler: PromptAssembler = PromptAssembler()

    val embeddingEngine: FakeEmbeddingEngine = FakeEmbeddingEngine()
    
    val textChunker: TextChunker = TextChunker()

    val knowledgeRepository: KnowledgeRepository = KnowledgeRepository(
        knowledgeDao = database.knowledgeDao(),
        embeddingEngine = embeddingEngine,
        textChunker = textChunker
    )

    val workflowRepository: WorkflowRepository = WorkflowRepository(
        context = applicationContext!!,
        workflowDao = database.workflowDao()
    )

    val sttEngine: AndroidSpeechToTextEngine = AndroidSpeechToTextEngine(context = applicationContext!!)
    val ttsEngine: AndroidTextToSpeechEngine = AndroidTextToSpeechEngine(context = applicationContext!!)
    val voiceRepository: VoiceRepository = VoiceRepository(voiceDao = database.voiceDao())

    init {
        toolRegistry.register(SearchKnowledgeTool.create(knowledgeRepository))
    }

    private val skillActivator: SkillActivator = SkillActivator(
        skillDao = database.skillDao(),
        promptDao = database.promptDao(),
        toolRegistry = toolRegistry,
    )

    private val modelRouter: ModelRouter = ModelRouter(
        providerRepository = providerRepository,
    )

    val agentRuntime: AgentRuntime = DefaultAgentRuntime(
        agentDao = database.agentDao(),
        conversationDao = database.conversationDao(),
        providerConfigDao = database.providerConfigDao(),
        debugTraceDao = database.debugTraceDao(),
        clientFactory = chatModelClientFactory,
        appSettingsRepository = appSettingsRepository,
        skillActivator = skillActivator,
        modelRouter = modelRouter,
        promptAssembler = promptAssembler,
        localModelEngine = localModelEngine,
    )

    val libraryRepository: LibraryRepository = LibraryRepository(
        agentDao = database.agentDao(),
        promptDao = database.promptDao(),
        skillDao = database.skillDao(),
        toolConfigDao = database.toolConfigDao(),
    )

    val appSettingsViewModelFactory: AppSettingsViewModel.Factory =
        AppSettingsViewModel.Factory(appSettingsRepository)

    val libraryViewModelFactory: LibraryViewModel.Factory =
        LibraryViewModel.Factory(libraryRepository)

    val localModelManagerViewModelFactory: LocalModelManagerViewModel.Factory =
        LocalModelManagerViewModel.Factory(localModelManager)

    val knowledgeViewModelFactory: KnowledgeViewModel.Factory =
        KnowledgeViewModel.Factory(knowledgeRepository)

    val workflowViewModelFactory: WorkflowViewModel.Factory =
        WorkflowViewModel.Factory(workflowRepository)

    val debugViewModelFactory: DebugViewModel.Factory =
        DebugViewModel.Factory(database.debugTraceDao())

    val chatViewModelFactory: ChatViewModel.Factory =
        ChatViewModel.Factory(
            conversationDao = database.conversationDao(),
            agentDao = database.agentDao(),
            agentRuntime = agentRuntime,
            sttEngine = sttEngine,
            ttsEngine = ttsEngine
        )
}
