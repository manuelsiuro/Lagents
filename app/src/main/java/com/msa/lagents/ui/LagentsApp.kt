package com.msa.lagents.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msa.lagents.core.di.AppContainer
import com.msa.lagents.ui.chat.ChatViewModel
import com.msa.lagents.ui.debug.DebugViewModel
import com.msa.lagents.ui.knowledge.KnowledgeViewModel
import com.msa.lagents.ui.library.AgentFormDialog
import com.msa.lagents.ui.library.LibraryViewModel
import com.msa.lagents.ui.library.PromptFormDialog
import com.msa.lagents.ui.library.SkillFormDialog
import com.msa.lagents.ui.library.ToolConfigFormDialog
import com.msa.lagents.ui.models.ModelsViewModel
import com.msa.lagents.ui.navigation.LagentsNavScaffold
import com.msa.lagents.ui.settings.AppSettingsViewModel
import com.msa.lagents.ui.theme.LagentsTheme

@Composable
fun LagentsApp(
    appContainer: AppContainer,
    windowSizeClass: WindowSizeClass,
) {
    val settingsViewModel: AppSettingsViewModel = viewModel(
        factory = appContainer.appSettingsViewModelFactory,
    )
    val settings by settingsViewModel.uiState.collectAsState()

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = appContainer.libraryViewModelFactory,
    )
    val libraryState by libraryViewModel.uiState.collectAsState()

    val chatViewModel: ChatViewModel = viewModel(
        factory = appContainer.chatViewModelFactory,
    )
    val chatState by chatViewModel.uiState.collectAsState()

    val modelsViewModel: ModelsViewModel = viewModel(
        factory = appContainer.modelsViewModelFactory,
    )
    val modelsState by modelsViewModel.uiState.collectAsState()

    val knowledgeViewModel: KnowledgeViewModel = viewModel(
        factory = appContainer.knowledgeViewModelFactory,
    )
    val knowledgeState by knowledgeViewModel.uiState.collectAsState()

    val workflowViewModel: com.msa.lagents.ui.workflows.WorkflowViewModel = viewModel(
        factory = appContainer.workflowViewModelFactory,
    )
    val workflowState by workflowViewModel.uiState.collectAsState()

    val debugViewModel: DebugViewModel = viewModel(
        factory = appContainer.debugViewModelFactory,
    )
    val debugState by debugViewModel.uiState.collectAsState()

    val editingAgent by libraryViewModel.editingAgent.collectAsState()
    val editingPrompt by libraryViewModel.editingPrompt.collectAsState()
    val editingSkill by libraryViewModel.editingSkill.collectAsState()
    val editingToolConfig by libraryViewModel.editingToolConfig.collectAsState()

    LagentsTheme(
        themePreference = settings.themePreference,
        dynamicColor = settings.dynamicColorEnabled,
    ) {
        LagentsNavScaffold(
            settings = settings,
            libraryState = libraryState,
            chatState = chatState,
            modelsState = modelsState,
            knowledgeState = knowledgeState,
            workflowState = workflowState,
            debugState = debugState,
            windowSizeClass = windowSizeClass,
            onCycleTheme = settingsViewModel::cycleThemePreference,
            onDynamicColorChanged = settingsViewModel::setDynamicColorEnabled,
            onLocalOnlyModeChanged = settingsViewModel::toggleLocalOnlyMode,
            onSensitiveTextRedactionChanged = settingsViewModel::setSensitiveTextRedactionEnabled,
            onRequireApprovalForSideEffectsChanged = settingsViewModel::setRequireApprovalForSideEffects,
            onCycleRoutingPreference = settingsViewModel::cycleRoutingPreference,
            onBudgetWarningsChanged = settingsViewModel::setBudgetWarningsEnabled,
            onCycleVoiceInputMode = settingsViewModel::cycleVoiceInputMode,
            onAutoReadAssistantResponsesChanged = settingsViewModel::setAutoReadAssistantResponses,
            onCycleTranscriptRetention = settingsViewModel::cycleTranscriptRetention,
            onCreateStarterAgent = libraryViewModel::createStarterAgent,
            onCreateStarterPrompt = libraryViewModel::createStarterPrompt,
            onCreateStarterSkill = libraryViewModel::createStarterSkill,
            onCreateStarterToolConfig = libraryViewModel::createStarterToolConfig,
            onArchiveAgent = libraryViewModel::archiveAgent,
            onDuplicateAgent = libraryViewModel::duplicateAgent,
            onDeleteAgent = libraryViewModel::deleteAgent,
            onArchivePrompt = libraryViewModel::archivePrompt,
            onDuplicatePrompt = libraryViewModel::duplicatePrompt,
            onDeletePrompt = libraryViewModel::deletePrompt,
            onArchiveSkill = libraryViewModel::archiveSkill,
            onDuplicateSkill = libraryViewModel::duplicateSkill,
            onDeleteSkill = libraryViewModel::deleteSkill,
            onDeleteToolConfig = libraryViewModel::deleteToolConfig,
            onEditAgent = libraryViewModel::startEditingAgent,
            onEditPrompt = libraryViewModel::startEditingPrompt,
            onEditSkill = libraryViewModel::startEditingSkill,
            onEditToolConfig = libraryViewModel::startEditingToolConfig,
            onSendMessage = chatViewModel::sendMessage,
            onSelectAgent = chatViewModel::selectAgent,
            onApproveTool = chatViewModel::approveTool,
            onAcceptMemory = chatViewModel::acceptMemorySuggestion,
            onDismissWorkflow = chatViewModel::dismissWorkflowProgress,
            onStartVoice = chatViewModel::startVoiceInput,
            onStopVoice = chatViewModel::stopVoiceInput,
            onTogglePlayback = chatViewModel::togglePlayback,
            onSelectConversation = chatViewModel::selectConversation,
            onDeleteConversation = chatViewModel::deleteConversation,
            onRenameConversation = chatViewModel::renameConversation,
            onRegisterMockModel = modelsViewModel::registerMockLocalModel,
            onLoadLocalModel = modelsViewModel::loadLocalModel,
            onUnloadLocalModel = modelsViewModel::unloadLocalModel,
            onAddProvider = modelsViewModel::addProvider,
            onDeleteProvider = modelsViewModel::deleteProvider,
            onCreateKnowledgeCollection = knowledgeViewModel::createCollection,
            onDeleteKnowledgeCollection = knowledgeViewModel::deleteCollection,
            onSelectKnowledgeCollection = knowledgeViewModel::selectCollection,
            onImportKnowledgeDocument = knowledgeViewModel::importDocument,
            onDeleteKnowledgeDocument = knowledgeViewModel::deleteDocument,
            onKnowledgeSearchQueryChanged = knowledgeViewModel::onSearchQueryChanged,
            onCreateWorkflow = workflowViewModel::createWorkflow,
            onStartWorkflow = workflowViewModel::startWorkflow,
            onDeleteWorkflow = workflowViewModel::deleteWorkflow,
            onSelectWorkflow = workflowViewModel::selectWorkflow,
            onProvideWorkflowApproval = workflowViewModel::provideApproval,
        )

        editingAgent?.let { agent ->
            AgentFormDialog(
                agent = agent,
                onDismiss = libraryViewModel::stopEditingAgent,
                onConfirm = libraryViewModel::updateAgent,
            )
        }
        editingPrompt?.let { prompt ->
            PromptFormDialog(
                prompt = prompt,
                onDismiss = libraryViewModel::stopEditingPrompt,
                onConfirm = libraryViewModel::updatePrompt,
            )
        }
        editingSkill?.let { skill ->
            SkillFormDialog(
                skill = skill,
                onDismiss = libraryViewModel::stopEditingSkill,
                onConfirm = libraryViewModel::updateSkill,
            )
        }
        editingToolConfig?.let { config ->
            ToolConfigFormDialog(
                toolConfig = config,
                onDismiss = libraryViewModel::stopEditingToolConfig,
                onConfirm = libraryViewModel::updateToolConfig,
            )
        }
    }
}
