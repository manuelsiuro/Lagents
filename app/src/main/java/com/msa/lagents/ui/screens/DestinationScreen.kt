package com.msa.lagents.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.lagents.data.library.LibraryOverview
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.tool.ToolConfigEntity
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.PrivacyMode
import com.msa.lagents.data.settings.ThemePreference
import com.msa.lagents.ui.chat.ChatScreen
import com.msa.lagents.ui.chat.ChatUiState
import com.msa.lagents.ui.components.LagentsEmptyState
import com.msa.lagents.ui.debug.DebugUiState
import com.msa.lagents.ui.knowledge.KnowledgeUiState
import com.msa.lagents.ui.models.ModelsUiState
import com.msa.lagents.ui.models.ProviderFormDialog
import com.msa.lagents.ui.navigation.LagentsDestination
import com.msa.lagents.ui.workflows.WorkflowUiState
import java.io.InputStream

@Composable
fun DestinationScreen(
    destination: LagentsDestination,
    settings: AppSettings,
    libraryState: LibraryOverview,
    chatState: ChatUiState,
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
    modelsState: ModelsUiState,
    onRegisterMockModel: () -> Unit,
    onLoadLocalModel: (String) -> Unit,
    onUnloadLocalModel: () -> Unit,
    onAddProvider: (String, String, String, String?) -> Unit,
    onDeleteProvider: (String) -> Unit,
    knowledgeState: KnowledgeUiState,
    onCreateKnowledgeCollection: (String, String) -> Unit,
    onDeleteKnowledgeCollection: (String) -> Unit,
    onSelectKnowledgeCollection: (String?) -> Unit,
    onImportKnowledgeDocument: (String, String, InputStream) -> Unit,
    onDeleteKnowledgeDocument: (String) -> Unit,
    onKnowledgeSearchQueryChanged: (String) -> Unit,
    debugState: DebugUiState,
    workflowState: WorkflowUiState,
    onCreateWorkflow: (String, String, String) -> Unit,
    onStartWorkflow: (String) -> Unit,
    onDeleteWorkflow: (String) -> Unit,
    onSelectWorkflow: (String?) -> Unit,
    onProvideWorkflowApproval: (String, Boolean) -> Unit,
    isTwoPane: Boolean = false,
    onSelectConversation: (String?) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onRenameConversation: (String, String) -> Unit,
) {
    when (destination) {
        LagentsDestination.Chat -> ChatScreen(
            state = chatState,
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
            isTwoPane = isTwoPane,
        )
        LagentsDestination.Library -> LibraryFoundationScreen(
            libraryState = libraryState,
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
        )
        LagentsDestination.Workflows -> WorkflowsFoundationScreen(
            state = workflowState,
            onCreate = onCreateWorkflow,
            onStart = onStartWorkflow,
            onDelete = onDeleteWorkflow,
            onSelect = onSelectWorkflow,
            onProvideApproval = onProvideWorkflowApproval,
        )
        LagentsDestination.Knowledge -> KnowledgeFoundationScreen(
            state = knowledgeState,
            onCreate = onCreateKnowledgeCollection,
            onDelete = onDeleteKnowledgeCollection,
            onSelect = onSelectKnowledgeCollection,
            onImport = onImportKnowledgeDocument,
            onDeleteDocument = onDeleteKnowledgeDocument,
            onSearchQueryChanged = onKnowledgeSearchQueryChanged,
        )
        LagentsDestination.Models -> ModelsFoundationScreen(
            state = modelsState,
            onRegisterMockLocal = onRegisterMockModel,
            onLoadLocal = onLoadLocalModel,
            onUnloadLocal = onUnloadLocalModel,
            onAddProvider = onAddProvider,
            onDeleteProvider = onDeleteProvider,
        )
        LagentsDestination.Debug -> DebugFoundationScreen(
            state = debugState,
        )
        LagentsDestination.Settings -> SettingsFoundationScreen(
            settings = settings,
            onDynamicColorChanged = onDynamicColorChanged,
            onLocalOnlyModeChanged = onLocalOnlyModeChanged,
            onSensitiveTextRedactionChanged = onSensitiveTextRedactionChanged,
            onRequireApprovalForSideEffectsChanged = onRequireApprovalForSideEffectsChanged,
            onCycleRoutingPreference = onCycleRoutingPreference,
            onBudgetWarningsChanged = onBudgetWarningsChanged,
            onCycleVoiceInputMode = onCycleVoiceInputMode,
            onAutoReadAssistantResponsesChanged = onAutoReadAssistantResponsesChanged,
            onCycleTranscriptRetention = onCycleTranscriptRetention,
        )
    }
}

@Composable
private fun LibraryFoundationScreen(
    libraryState: LibraryOverview,
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
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "Asset library",
                summary = "Create and manage agents, reusable prompt templates, skills (bundled tools), and tool configurations.",
            )
        }

        item {
            LibrarySection(
                title = "Agents",
                body = "Persona and behavior definitions.",
                actionLabel = "New Agent",
                onAction = onCreateStarterAgent,
                isEmpty = libraryState.agents.isEmpty(),
                emptyTitle = "No agents",
            ) {
                libraryState.agents.forEach { agent ->
                    AssetRow(
                        title = agent.name,
                        body = agent.systemBehavior,
                        onArchive = { onArchiveAgent(agent.id) },
                        onDuplicate = { onDuplicateAgent(agent.id) },
                        onDelete = { onDeleteAgent(agent.id) },
                        onEdit = { onEditAgent(agent.id) }
                    )
                }
            }
        }

        item {
            LibrarySection(
                title = "Prompts",
                body = "Reusable system and user message templates.",
                actionLabel = "New Prompt",
                onAction = onCreateStarterPrompt,
                isEmpty = libraryState.prompts.isEmpty(),
                emptyTitle = "No prompts",
            ) {
                libraryState.prompts.forEach { prompt ->
                    AssetRow(
                        title = prompt.title,
                        body = prompt.description,
                        onArchive = { onArchivePrompt(prompt.id) },
                        onDuplicate = { onDuplicatePrompt(prompt.id) },
                        onDelete = { onDeletePrompt(prompt.id) },
                        onEdit = { onEditPrompt(prompt.id) }
                    )
                }
            }
        }

        item {
            LibrarySection(
                title = "Skills",
                body = "Bundled prompt and tool sets.",
                actionLabel = "New Skill",
                onAction = onCreateStarterSkill,
                isEmpty = libraryState.skills.isEmpty(),
                emptyTitle = "No skills",
            ) {
                libraryState.skills.forEach { skill ->
                    AssetRow(
                        title = skill.title,
                        body = skill.instructions,
                        onArchive = { onArchiveSkill(skill.id) },
                        onDuplicate = { onDuplicateSkill(skill.id) },
                        onDelete = { onDeleteSkill(skill.id) },
                        onEdit = { onEditSkill(skill.id) }
                    )
                }
            }
        }

        item {
            LibrarySection(
                title = "Tools",
                body = "Platform and external tool settings.",
                actionLabel = "New Config",
                onAction = onCreateStarterToolConfig,
                isEmpty = libraryState.tools.isEmpty(),
                emptyTitle = "No tool configurations",
            ) {
                libraryState.tools.forEach { tool ->
                    AssetRow(
                        title = tool.displayName,
                        body = "Enabled: ${tool.enabled}",
                        onDelete = { onDeleteToolConfig(tool.id) },
                        onEdit = { onEditToolConfig(tool.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkflowsFoundationScreen(
    state: WorkflowUiState,
    onCreate: (String, String, String) -> Unit,
    onStart: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSelect: (String?) -> Unit,
    onProvideApproval: (String, Boolean) -> Unit,
) {
    if (state.selectedWorkflowId != null) {
        val selectedWorkflow = state.definitions.find { it.id == state.selectedWorkflowId }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onSelect(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        selectedWorkflow?.name ?: "Workflow",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            item {
                LibrarySection(
                    title = "Runs",
                    body = "Execution history of this workflow.",
                    actionLabel = "Start New Run",
                    onAction = { onStart(state.selectedWorkflowId!!) },
                    isEmpty = state.runs.isEmpty(),
                    emptyTitle = "No runs yet",
                ) {
                    state.runs.forEach { run ->
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssetRow(
                                title = "Run ${run.startedAtMillis}",
                                body = "Status: ${run.status} • Progress: ${(run.progress * 100).toInt()}%",
                                onDelete = { /* TODO */ }
                            )
                            
                            if (run.status == "Awaiting Approval") {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Approval Required", style = MaterialTheme.typography.titleSmall)
                                        Text("Tool: ${run.pendingApprovalToolName}", style = MaterialTheme.typography.bodySmall)
                                        Text("Arguments: ${run.pendingApprovalArguments}", style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = { onProvideApproval(run.id, false) }) { Text("Deny") }
                                            Button(onClick = { onProvideApproval(run.id, true) }) { Text("Approve") }
                                        }
                                    }
                                }
                            }
                            
                            if (run.logs.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = run.logs,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Header(
                    title = "Autonomous workflows",
                    summary = "Define goals for your agents and let them work in the background. Monitor progress, review results, and handle approval gates.",
                )
            }
            
            item {
                LibrarySection(
                    title = "My Workflows",
                    body = "Active background tasks and scheduled routines.",
                    actionLabel = "New Workflow",
                    onAction = { onCreate("Research Task", "Research the latest news on AI", "agent-1") },
                    isEmpty = state.definitions.isEmpty(),
                    emptyTitle = "No workflows",
                ) {
                    state.definitions.forEach { workflow ->
                        AssetRow(
                            title = workflow.name,
                            body = workflow.goal,
                            onDelete = { onDelete(workflow.id) },
                            onClick = { onSelect(workflow.id) }
                        )
                    }
                }
            }

            item {
                FoundationCard(
                    item = FoundationItem(
                        title = "Background Execution",
                        body = "Workflows run reliably in the background using Android WorkManager.",
                        icon = Icons.Outlined.Route
                    )
                )
            }
        }
    }
}

@Composable
private fun KnowledgeFoundationScreen(
    state: KnowledgeUiState,
    onCreate: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onSelect: (String?) -> Unit,
    onImport: (String, String, InputStream) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            val title = it.path?.substringAfterLast('/') ?: "New Document"
            context.contentResolver.openInputStream(it)?.let { stream ->
                onImport(title, it.toString(), stream)
            }
        }
    }

    if (state.selectedCollectionId != null) {
        val selectedCollection = state.collections.find { it.id == state.selectedCollectionId }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onSelect(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        selectedCollection?.name ?: "Collection",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search this collection") },
                    placeholder = { Text("Ask a question about your documents...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
            }

            if (state.searchResults.isNotEmpty()) {
                item {
                    Text("Search Results", style = MaterialTheme.typography.titleMedium)
                }
                items(state.searchResults) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Score: ${(result.score * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = result.chunk.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                LibrarySection(
                    title = "Documents",
                    body = "Documents in this collection.",
                    actionLabel = "Import File",
                    onAction = { filePickerLauncher.launch(arrayOf("text/plain", "text/markdown")) },
                    isEmpty = state.documents.isEmpty(),
                    emptyTitle = "No documents",
                ) {
                    state.documents.forEach { doc ->
                        AssetRow(
                            title = doc.title,
                            body = "Status: ${doc.status}",
                            onDelete = { onDeleteDocument(doc.id) }
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Header(
                    title = "Private knowledge",
                    summary = "Local RAG allows you to import documents and search them privately on-device. Collections are indexed and stored only on your phone.",
                )
            }
            
            item {
                LibrarySection(
                    title = "Collections",
                    body = "Searchable groups of documents.",
                    actionLabel = "New Collection",
                    onAction = { onCreate("My Documents", "Private research and notes") },
                    isEmpty = state.collections.isEmpty(),
                    emptyTitle = "No collections",
                ) {
                    state.collections.forEach { collection ->
                        AssetRow(
                            title = collection.name,
                            body = collection.description,
                            onDelete = { onDelete(collection.id) },
                            onClick = { onSelect(collection.id) }
                        )
                    }
                }
            }

            item {
                FoundationCard(
                    item = FoundationItem(
                        title = "Document Extraction",
                        body = "Support for PDF, Markdown, and Text files is coming soon.",
                        icon = Icons.Outlined.Route
                    )
                )
            }
        }
    }
}

@Composable
private fun ModelsFoundationScreen(
    state: ModelsUiState,
    onRegisterMockLocal: () -> Unit,
    onLoadLocal: (String) -> Unit,
    onUnloadLocal: () -> Unit,
    onAddProvider: (String, String, String, String?) -> Unit,
    onDeleteProvider: (String) -> Unit,
) {
    var showProviderDialog by remember { mutableStateOf(false) }

    if (showProviderDialog) {
        ProviderFormDialog(
            onDismiss = { showProviderDialog = false },
            onConfirm = { type, name, key, url ->
                onAddProvider(type, name, key, url)
                showProviderDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "Models and providers",
                summary = "Manage cloud API keys and on-device local models. Use 'Local-only' mode in settings to force private inference.",
            )
        }
        
        item {
            LibrarySection(
                title = "Cloud Providers",
                body = "Configure API keys for OpenAI, Anthropic, Gemini, etc.",
                actionLabel = "Add Provider",
                onAction = { showProviderDialog = true },
                isEmpty = state.cloudProviders.isEmpty(),
                emptyTitle = "No providers",
            ) {
                state.cloudProviders.forEach { provider ->
                    AssetRow(
                        title = provider.displayName,
                        body = "Type: ${provider.providerType}",
                        onDelete = { onDeleteProvider(provider.id) }
                    )
                }
            }
        }

        item {
            LibrarySection(
                title = "Local Models",
                body = "On-device LLMs for private, offline inference.",
                actionLabel = "Add Mock",
                onAction = onRegisterMockLocal,
                isEmpty = state.localModels.isEmpty(),
                emptyTitle = "No local models",
            ) {
                state.localModels.forEach { model ->
                    val isActive = model.id == state.activeLocalModelId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                                Text("${model.engine} • ${(model.sizeBytes / 1_000_000)} MB", style = MaterialTheme.typography.bodySmall)
                            }
                            if (isActive) {
                                Button(onClick = onUnloadLocal) { Text("Unload") }
                            } else {
                                OutlinedButton(onClick = { onLoadLocal(model.id) }) { Text("Load") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugFoundationScreen(
    state: DebugUiState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "Debug console",
                summary = "Inspect every agent interaction, including prompts, tool calls, and usage metrics.",
            )
        }

        item {
            LibrarySection(
                title = "Recent Traces",
                body = "Logs of recent generations.",
                actionLabel = "Clear All",
                onAction = { /* TODO */ },
                isEmpty = state.recentTraces.isEmpty(),
                emptyTitle = "No traces recorded",
            ) {
                state.recentTraces.forEach { trace ->
                    AssetRow(
                        title = "Trace ${trace.createdAtMillis}",
                        body = "Model: ${trace.modelId} • Prompt: ${trace.promptPreview}",
                        onDelete = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsFoundationScreen(
    settings: AppSettings,
    onDynamicColorChanged: (Boolean) -> Unit,
    onLocalOnlyModeChanged: (Boolean) -> Unit,
    onSensitiveTextRedactionChanged: (Boolean) -> Unit,
    onRequireApprovalForSideEffectsChanged: (Boolean) -> Unit,
    onCycleRoutingPreference: () -> Unit,
    onBudgetWarningsChanged: (Boolean) -> Unit,
    onCycleVoiceInputMode: () -> Unit,
    onAutoReadAssistantResponsesChanged: (Boolean) -> Unit,
    onCycleTranscriptRetention: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "App settings",
                summary = "Control your privacy, budget, routing, and voice experience. These settings are stored locally on your device.",
            )
        }

        item {
            Text(
                "Privacy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SettingRow(
                title = "Local-only mode",
                summary = "Block all cloud provider calls and force on-device inference.",
                checked = settings.privacyMode == PrivacyMode.LocalOnly,
                onCheckedChange = { onLocalOnlyModeChanged(it) }
            )
        }

        item {
            SettingRow(
                title = "Sensitive text redaction",
                summary = "Attempt to remove PII from prompts before sending to cloud providers.",
                checked = settings.sensitiveTextRedactionEnabled,
                onCheckedChange = onSensitiveTextRedactionChanged
            )
        }

        item {
            SettingRow(
                title = "Require tool approval",
                summary = "Pause and ask for confirmation before executing side-effectful tools.",
                checked = settings.requireApprovalForSideEffects,
                onCheckedChange = onRequireApprovalForSideEffectsChanged
            )
        }

        item {
            Text(
                "Model Routing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            ChoiceRow(
                title = "Routing preference",
                summary = "Choose how the app selects between available models.",
                value = settings.routingPreference.displayName,
                onCycle = onCycleRoutingPreference
            )
        }

        item {
            SettingRow(
                title = "Budget warnings",
                summary = "Show alerts before using expensive cloud models.",
                checked = settings.budgetWarningsEnabled,
                onCheckedChange = onBudgetWarningsChanged
            )
        }

        item {
            Text(
                "Voice & Speech",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            ChoiceRow(
                title = "Voice input mode",
                summary = "Control how speech is captured and processed.",
                value = settings.voiceInputMode.displayName,
                onCycle = onCycleVoiceInputMode
            )
        }

        item {
            SettingRow(
                title = "Auto-read responses",
                summary = "Automatically read assistant responses using Text-to-Speech.",
                checked = settings.autoReadAssistantResponses,
                onCheckedChange = onAutoReadAssistantResponsesChanged
            )
        }

        item {
            ChoiceRow(
                title = "Transcript retention",
                summary = "Control how long voice transcripts are stored.",
                value = settings.transcriptRetention.displayName,
                onCycle = onCycleTranscriptRetention
            )
        }

        item {
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SettingRow(
                title = "Dynamic color",
                summary = "Use colors derived from your wallpaper (Android 12+).",
                checked = settings.dynamicColorEnabled,
                onCheckedChange = onDynamicColorChanged
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    summary: String,
    value: String,
    onCycle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onCycle)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun Header(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FoundationCard(item: FoundationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    isEmpty: Boolean,
    emptyTitle: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }

        if (isEmpty) {
            LagentsEmptyState(
                title = emptyTitle,
                body = body,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AssetRow(
    title: String,
    body: String,
    onArchive: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Asset options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                    }
                    if (onDuplicate != null) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { onDuplicate(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }
                        )
                    }
                    if (onArchive != null) {
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = { onArchive(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Archive, null) }
                        )
                    }
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

data class FoundationItem(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

private val Enum<*>.displayName: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
