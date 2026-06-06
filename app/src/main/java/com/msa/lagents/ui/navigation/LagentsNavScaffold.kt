package com.msa.lagents.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msa.lagents.data.library.LibraryOverview
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.ThemePreference
import com.msa.lagents.ui.chat.ChatUiState
import com.msa.lagents.ui.debug.DebugUiState
import com.msa.lagents.ui.knowledge.KnowledgeUiState
import com.msa.lagents.ui.models.ModelsUiState
import com.msa.lagents.ui.screens.DestinationScreen
import com.msa.lagents.ui.workflows.WorkflowUiState
import java.io.InputStream

@Composable
fun LagentsNavScaffold(
    settings: AppSettings,
    libraryState: LibraryOverview,
    chatState: ChatUiState,
    modelsState: ModelsUiState,
    knowledgeState: KnowledgeUiState,
    workflowState: WorkflowUiState,
    debugState: DebugUiState,
    windowSizeClass: WindowSizeClass,
    onCycleTheme: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onLocalOnlyModeChanged: (Boolean) -> Unit,
    onSensitiveTextRedactionChanged: (Boolean) -> Unit,
    onRequireApprovalForSideEffectsChanged: (Boolean) -> Unit,
    onCycleRoutingPreference: () -> Unit,
    onBudgetWarningsChanged: (Boolean) -> Unit,
    onCycleVoiceInputMode: () -> Unit,
    onAutoReadAssistantResponsesChanged: (Boolean) -> Unit,
    onCycleTranscriptRetention: () -> Unit,
    onCreateStarterAgent: () -> Unit,
    onCreateStarterPrompt: () -> Unit,
    onCreateStarterSkill: () -> Unit,
    onCreateStarterToolConfig: () -> Unit,
    onArchiveAgent: (String) -> Unit,
    onDuplicateAgent: (String) -> Unit,
    onDeleteAgent: (String) -> Unit,
    onArchivePrompt: (String) -> Unit,
    onDuplicatePrompt: (String) -> Unit,
    onDeletePrompt: (String) -> Unit,
    onArchiveSkill: (String) -> Unit,
    onDuplicateSkill: (String) -> Unit,
    onDeleteSkill: (String) -> Unit,
    onDeleteToolConfig: (String) -> Unit,
    onEditAgent: (String) -> Unit,
    onEditPrompt: (String) -> Unit,
    onEditSkill: (String) -> Unit,
    onEditToolConfig: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSelectAgent: (String) -> Unit,
    onApproveTool: (Boolean) -> Unit,
    onAcceptMemory: (String, Boolean) -> Unit,
    onDismissWorkflow: () -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onTogglePlayback: (String) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onRegisterMockModel: () -> Unit,
    onLoadLocalModel: (String) -> Unit,
    onUnloadLocalModel: () -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onAddProvider: (String, String, String, String?) -> Unit,
    onDeleteProvider: (String) -> Unit,
    onCreateKnowledgeCollection: (String, String) -> Unit,
    onDeleteKnowledgeCollection: (String) -> Unit,
    onSelectKnowledgeCollection: (String?) -> Unit,
    onImportKnowledgeDocument: (String, String, InputStream) -> Unit,
    onDeleteKnowledgeDocument: (String) -> Unit,
    onKnowledgeSearchQueryChanged: (String) -> Unit,
    onCreateWorkflow: (String, String, String) -> Unit,
    onStartWorkflow: (String) -> Unit,
    onDeleteWorkflow: (String) -> Unit,
    onSelectWorkflow: (String?) -> Unit,
    onProvideWorkflowApproval: (String, Boolean) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedDestination = LagentsTopLevelDestinations.find { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    } ?: LagentsDestination.Chat

    val isWideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val isTwoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            LagentsNavigationRail(
                selectedDestination = selectedDestination,
                onNavigate = { navController.navigateTopLevel(it) }
            )
            Scaffold(
                topBar = {
                    LagentsTopBar(
                        destination = selectedDestination,
                        themePreference = settings.themePreference,
                        onCycleTheme = onCycleTheme
                    )
                }
            ) { innerPadding ->
                LagentsNavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    settings = settings,
                    libraryState = libraryState,
                    chatState = chatState,
                    modelsState = modelsState,
                    knowledgeState = knowledgeState,
                    workflowState = workflowState,
                    debugState = debugState,
                    isTwoPane = isTwoPane,
                    onDynamicColorChanged = onDynamicColorChanged,
                    onLocalOnlyModeChanged = onLocalOnlyModeChanged,
                    onSensitiveTextRedactionChanged = onSensitiveTextRedactionChanged,
                    onRequireApprovalForSideEffectsChanged = onRequireApprovalForSideEffectsChanged,
                    onCycleRoutingPreference = onCycleRoutingPreference,
                    onBudgetWarningsChanged = onBudgetWarningsChanged,
                    onCycleVoiceInputMode = onCycleVoiceInputMode,
                    onAutoReadAssistantResponsesChanged = onAutoReadAssistantResponsesChanged,
                    onCycleTranscriptRetention = onCycleTranscriptRetention,
                    onCreateStarterAgent = onCreateStarterAgent,
                    onCreateStarterPrompt = onCreateStarterPrompt,
                    onCreateStarterSkill = onCreateStarterSkill,
                    onCreateStarterToolConfig = onCreateStarterToolConfig,
                    onArchiveAgent = onArchiveAgent,
                    onDuplicateAgent = onDuplicateAgent,
                    onDeleteAgent = onDeleteAgent,
                    onArchivePrompt = onArchivePrompt,
                    onDuplicatePrompt = onDuplicatePrompt,
                    onDeletePrompt = onDeletePrompt,
                    onArchiveSkill = onArchiveSkill,
                    onDuplicateSkill = onDuplicateSkill,
                    onDeleteSkill = onDeleteSkill,
                    onDeleteToolConfig = onDeleteToolConfig,
                    onEditAgent = onEditAgent,
                    onEditPrompt = onEditPrompt,
                    onEditSkill = onEditSkill,
                    onEditToolConfig = onEditToolConfig,
                    onSendMessage = onSendMessage,
                    onSelectAgent = onSelectAgent,
                    onApproveTool = onApproveTool,
                    onAcceptMemory = onAcceptMemory,
                    onDismissWorkflow = onDismissWorkflow,
                    onStartVoice = onStartVoice,
                    onStopVoice = onStopVoice,
                    onTogglePlayback = onTogglePlayback,
                    onSelectConversation = onSelectConversation,
                    onDeleteConversation = onDeleteConversation,
                    onRenameConversation = onRenameConversation,
                    onRegisterMockModel = onRegisterMockModel,
                    onLoadLocalModel = onLoadLocalModel,
                    onUnloadLocalModel = onUnloadLocalModel,
                    onDownloadLocalModel = onDownloadLocalModel,
                    onDeleteLocalModel = onDeleteLocalModel,
                    onAddProvider = onAddProvider,
                    onDeleteProvider = onDeleteProvider,
                    onCreateKnowledgeCollection = onCreateKnowledgeCollection,
                    onDeleteKnowledgeCollection = onDeleteKnowledgeCollection,
                    onSelectKnowledgeCollection = onSelectKnowledgeCollection,
                    onImportKnowledgeDocument = onImportKnowledgeDocument,
                    onDeleteKnowledgeDocument = onDeleteKnowledgeDocument,
                    onKnowledgeSearchQueryChanged = onKnowledgeSearchQueryChanged,
                    onCreateWorkflow = onCreateWorkflow,
                    onStartWorkflow = onStartWorkflow,
                    onDeleteWorkflow = onDeleteWorkflow,
                    onSelectWorkflow = onSelectWorkflow,
                    onProvideWorkflowApproval = onProvideWorkflowApproval,
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                LagentsTopBar(
                    destination = selectedDestination,
                    themePreference = settings.themePreference,
                    onCycleTheme = onCycleTheme
                )
            },
            bottomBar = {
                LagentsNavigationBar(
                    selectedDestination = selectedDestination,
                    onNavigate = { navController.navigateTopLevel(it) }
                )
            }
        ) { innerPadding ->
            LagentsNavHost(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                settings = settings,
                libraryState = libraryState,
                chatState = chatState,
                modelsState = modelsState,
                knowledgeState = knowledgeState,
                workflowState = workflowState,
                debugState = debugState,
                isTwoPane = isTwoPane,
                onDynamicColorChanged = onDynamicColorChanged,
                onLocalOnlyModeChanged = onLocalOnlyModeChanged,
                onSensitiveTextRedactionChanged = onSensitiveTextRedactionChanged,
                onRequireApprovalForSideEffectsChanged = onRequireApprovalForSideEffectsChanged,
                onCycleRoutingPreference = onCycleRoutingPreference,
                onBudgetWarningsChanged = onBudgetWarningsChanged,
                onCycleVoiceInputMode = onCycleVoiceInputMode,
                onAutoReadAssistantResponsesChanged = onAutoReadAssistantResponsesChanged,
                onCycleTranscriptRetention = onCycleTranscriptRetention,
                onCreateStarterAgent = onCreateStarterAgent,
                onCreateStarterPrompt = onCreateStarterPrompt,
                onCreateStarterSkill = onCreateStarterSkill,
                onCreateStarterToolConfig = onCreateStarterToolConfig,
                onArchiveAgent = onArchiveAgent,
                onDuplicateAgent = onDuplicateAgent,
                onDeleteAgent = onDeleteAgent,
                onArchivePrompt = onArchivePrompt,
                onDuplicatePrompt = onDuplicatePrompt,
                onDeletePrompt = onDeletePrompt,
                onArchiveSkill = onArchiveSkill,
                onDuplicateSkill = onDuplicateSkill,
                onDeleteSkill = onDeleteSkill,
                onDeleteToolConfig = onDeleteToolConfig,
                onEditAgent = onEditAgent,
                onEditPrompt = onEditPrompt,
                onEditSkill = onEditSkill,
                onEditToolConfig = onEditToolConfig,
                onSendMessage = onSendMessage,
                onSelectAgent = onSelectAgent,
                onApproveTool = onApproveTool,
                onAcceptMemory = onAcceptMemory,
                onDismissWorkflow = onDismissWorkflow,
                onStartVoice = onStartVoice,
                onStopVoice = onStopVoice,
                onTogglePlayback = onTogglePlayback,
                onSelectConversation = onSelectConversation,
                onDeleteConversation = onDeleteConversation,
                onRenameConversation = onRenameConversation,
                onRegisterMockModel = onRegisterMockModel,
                onLoadLocalModel = onLoadLocalModel,
                onUnloadLocalModel = onUnloadLocalModel,
                onDownloadLocalModel = onDownloadLocalModel,
                onDeleteLocalModel = onDeleteLocalModel,
                onAddProvider = onAddProvider,
                onDeleteProvider = onDeleteProvider,
                onCreateKnowledgeCollection = onCreateKnowledgeCollection,
                onDeleteKnowledgeCollection = onDeleteKnowledgeCollection,
                onSelectKnowledgeCollection = onSelectKnowledgeCollection,
                onImportKnowledgeDocument = onImportKnowledgeDocument,
                onDeleteKnowledgeDocument = onDeleteKnowledgeDocument,
                onKnowledgeSearchQueryChanged = onKnowledgeSearchQueryChanged,
                onCreateWorkflow = onCreateWorkflow,
                onStartWorkflow = onStartWorkflow,
                onDeleteWorkflow = onDeleteWorkflow,
                onSelectWorkflow = onSelectWorkflow,
                onProvideWorkflowApproval = onProvideWorkflowApproval,
            )
        }
    }
}

@Composable
private fun LagentsNavHost(
    modifier: Modifier,
    navController: NavHostController,
    settings: AppSettings,
    libraryState: LibraryOverview,
    chatState: ChatUiState,
    modelsState: ModelsUiState,
    knowledgeState: KnowledgeUiState,
    workflowState: WorkflowUiState,
    debugState: DebugUiState,
    isTwoPane: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    onLocalOnlyModeChanged: (Boolean) -> Unit,
    onSensitiveTextRedactionChanged: (Boolean) -> Unit,
    onRequireApprovalForSideEffectsChanged: (Boolean) -> Unit,
    onCycleRoutingPreference: () -> Unit,
    onBudgetWarningsChanged: (Boolean) -> Unit,
    onCycleVoiceInputMode: () -> Unit,
    onAutoReadAssistantResponsesChanged: (Boolean) -> Unit,
    onCycleTranscriptRetention: () -> Unit,
    onCreateStarterAgent: () -> Unit,
    onCreateStarterPrompt: () -> Unit,
    onCreateStarterSkill: () -> Unit,
    onCreateStarterToolConfig: () -> Unit,
    onArchiveAgent: (String) -> Unit,
    onDuplicateAgent: (String) -> Unit,
    onDeleteAgent: (String) -> Unit,
    onArchivePrompt: (String) -> Unit,
    onDuplicatePrompt: (String) -> Unit,
    onDeletePrompt: (String) -> Unit,
    onArchiveSkill: (String) -> Unit,
    onDuplicateSkill: (String) -> Unit,
    onDeleteSkill: (String) -> Unit,
    onDeleteToolConfig: (String) -> Unit,
    onEditAgent: (String) -> Unit,
    onEditPrompt: (String) -> Unit,
    onEditSkill: (String) -> Unit,
    onEditToolConfig: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSelectAgent: (String) -> Unit,
    onApproveTool: (Boolean) -> Unit,
    onAcceptMemory: (String, Boolean) -> Unit,
    onDismissWorkflow: () -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onTogglePlayback: (String) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onRegisterMockModel: () -> Unit,
    onLoadLocalModel: (String) -> Unit,
    onUnloadLocalModel: () -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onAddProvider: (String, String, String, String?) -> Unit,
    onDeleteProvider: (String) -> Unit,
    onCreateKnowledgeCollection: (String, String) -> Unit,
    onDeleteKnowledgeCollection: (String) -> Unit,
    onSelectKnowledgeCollection: (String?) -> Unit,
    onImportKnowledgeDocument: (String, String, InputStream) -> Unit,
    onDeleteKnowledgeDocument: (String) -> Unit,
    onKnowledgeSearchQueryChanged: (String) -> Unit,
    onCreateWorkflow: (String, String, String) -> Unit,
    onStartWorkflow: (String) -> Unit,
    onDeleteWorkflow: (String) -> Unit,
    onSelectWorkflow: (String?) -> Unit,
    onProvideWorkflowApproval: (String, Boolean) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = LagentsDestination.Chat.route,
        ) {
            LagentsTopLevelDestinations.forEach { destination ->
                composable(destination.route) {
                    DestinationScreen(
                        destination = destination,
                        settings = settings,
                        libraryState = libraryState,
                        chatState = chatState,
                        onDynamicColorChanged = onDynamicColorChanged,
                        onLocalOnlyModeChanged = onLocalOnlyModeChanged,
                        onSensitiveTextRedactionChanged = onSensitiveTextRedactionChanged,
                        onRequireApprovalForSideEffectsChanged = onRequireApprovalForSideEffectsChanged,
                        onCycleRoutingPreference = onCycleRoutingPreference,
                        onBudgetWarningsChanged = onBudgetWarningsChanged,
                        onCycleVoiceInputMode = onCycleVoiceInputMode,
                        onAutoReadAssistantResponsesChanged = onAutoReadAssistantResponsesChanged,
                        onCycleTranscriptRetention = onCycleTranscriptRetention,
                        onCreateStarterAgent = onCreateStarterAgent,
                        onCreateStarterPrompt = onCreateStarterPrompt,
                        onCreateStarterSkill = onCreateStarterSkill,
                        onCreateStarterToolConfig = onCreateStarterToolConfig,
                        onArchiveAgent = onArchiveAgent,
                        onDuplicateAgent = onDuplicateAgent,
                        onDeleteAgent = onDeleteAgent,
                        onArchivePrompt = onArchivePrompt,
                        onDuplicatePrompt = onDuplicatePrompt,
                        onDeletePrompt = onDeletePrompt,
                        onArchiveSkill = onArchiveSkill,
                        onDuplicateSkill = onDuplicateSkill,
                        onDeleteSkill = onDeleteSkill,
                        onDeleteToolConfig = onDeleteToolConfig,
                        onEditAgent = onEditAgent,
                        onEditPrompt = onEditPrompt,
                        onEditSkill = onEditSkill,
                        onEditToolConfig = onEditToolConfig,
                        onSendMessage = onSendMessage,
                        onSelectAgent = onSelectAgent,
                        onApproveTool = onApproveTool,
                        onAcceptMemory = onAcceptMemory,
                        onDismissWorkflow = onDismissWorkflow,
                        onStartVoice = onStartVoice,
                        onStopVoice = onStopVoice,
                        onTogglePlayback = onTogglePlayback,
                        modelsState = modelsState,
                        onRegisterMockModel = onRegisterMockModel,
                        onLoadLocalModel = onLoadLocalModel,
                        onUnloadLocalModel = onUnloadLocalModel,
                        onDownloadLocalModel = onDownloadLocalModel,
                        onDeleteLocalModel = onDeleteLocalModel,
                        onAddProvider = onAddProvider,
                        onDeleteProvider = onDeleteProvider,
                        knowledgeState = knowledgeState,
                        onCreateKnowledgeCollection = onCreateKnowledgeCollection,
                        onDeleteKnowledgeCollection = onDeleteKnowledgeCollection,
                        onSelectKnowledgeCollection = onSelectKnowledgeCollection,
                        onImportKnowledgeDocument = onImportKnowledgeDocument,
                        onDeleteKnowledgeDocument = onDeleteKnowledgeDocument,
                        onKnowledgeSearchQueryChanged = onKnowledgeSearchQueryChanged,
                        debugState = debugState,
                        workflowState = workflowState,
                        onCreateWorkflow = onCreateWorkflow,
                        onStartWorkflow = onStartWorkflow,
                        onDeleteWorkflow = onDeleteWorkflow,
                        onSelectWorkflow = onSelectWorkflow,
                        onProvideWorkflowApproval = onProvideWorkflowApproval,
                        isTwoPane = isTwoPane,
                        onSelectConversation = onSelectConversation,
                        onDeleteConversation = onDeleteConversation,
                        onRenameConversation = onRenameConversation,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LagentsTopBar(
    destination: LagentsDestination,
    themePreference: ThemePreference,
    onCycleTheme: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = destination.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            IconButton(onClick = onCycleTheme) {
                Icon(
                    imageVector = when (themePreference) {
                        ThemePreference.System -> Icons.Default.Settings
                        ThemePreference.Light -> Icons.Default.Settings
                        ThemePreference.Dark -> Icons.Default.Settings
                    },
                    contentDescription = "Cycle theme"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun LagentsNavigationBar(
    selectedDestination: LagentsDestination,
    onNavigate: (LagentsDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        LagentsTopLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

@Composable
private fun LagentsNavigationRail(
    selectedDestination: LagentsDestination,
    onNavigate: (LagentsDestination) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    ) {
        LagentsTopLevelDestinations.forEach { destination ->
            NavigationRailItem(
                selected = selectedDestination == destination,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(destination: LagentsDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
