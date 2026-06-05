package com.msa.lagents.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.lagents.data.library.LibraryOverview
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.PrivacyMode
import com.msa.lagents.ui.components.LagentsEmptyState
import com.msa.lagents.ui.navigation.LagentsDestination

@Composable
fun DestinationScreen(
    destination: LagentsDestination,
    settings: AppSettings,
    libraryState: LibraryOverview,
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
) {
    when (destination) {
        LagentsDestination.Chat -> ChatFoundationScreen()
        LagentsDestination.Library -> LibraryFoundationScreen(
            libraryState = libraryState,
            onCreateStarterAgent = onCreateStarterAgent,
            onCreateStarterPrompt = onCreateStarterPrompt,
            onCreateStarterSkill = onCreateStarterSkill,
            onCreateStarterToolConfig = onCreateStarterToolConfig,
        )
        LagentsDestination.Workflows -> WorkflowsFoundationScreen()
        LagentsDestination.Knowledge -> KnowledgeFoundationScreen()
        LagentsDestination.Models -> ModelsFoundationScreen()
        LagentsDestination.Debug -> DebugFoundationScreen()
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
private fun ChatFoundationScreen() {
    FoundationScreen(
        title = "Agent workspace",
        summary = "The foundation is ready for streaming chat, skills, tools, local models, voice, and supervised workflows.",
        chips = listOf("OpenAI", "Anthropic", "Gemini", "Mistral", "Local"),
        items = listOf(
            FoundationItem(
                title = "Tool-using chat",
                body = "The chat surface will host model routing, approvals, citations, memory suggestions, and regenerate/stop controls.",
                icon = Icons.Outlined.AutoAwesome,
            ),
            FoundationItem(
                title = "Voice ready",
                body = "Speech-to-text and text-to-speech controls have a defined place in the shell and settings model.",
                icon = Icons.Outlined.GraphicEq,
            ),
            FoundationItem(
                title = "Manual wiring",
                body = "The app is connected through an explicit container, not generated dependency injection.",
                icon = Icons.Outlined.Hub,
            ),
        ),
    )
}

@Composable
private fun LibraryFoundationScreen(
    libraryState: LibraryOverview,
    onCreateStarterAgent: () -> Unit,
    onCreateStarterPrompt: () -> Unit,
    onCreateStarterSkill: () -> Unit,
    onCreateStarterToolConfig: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "Agent asset library",
                summary = "Create and manage the reusable building blocks for agents, prompts, skills, and tool configurations.",
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssetCountChip(label = "Agents", count = libraryState.agents.size)
                AssetCountChip(label = "Prompts", count = libraryState.prompts.size)
                AssetCountChip(label = "Skills", count = libraryState.skills.size)
                AssetCountChip(label = "Tools", count = libraryState.tools.size)
            }
        }
        item {
            LibrarySection(
                title = "Agents",
                body = "Profiles that combine behavior, model routing, skills, tools, memory, voice, and workflow permissions.",
                actionLabel = "New agent",
                onAction = onCreateStarterAgent,
                isEmpty = libraryState.agents.isEmpty(),
                emptyTitle = "No agents yet",
                emptyBody = "Create a draft agent profile to start wiring model routing, tools, and memory policy.",
            ) {
                libraryState.agents.forEach { agent ->
                    AssetRow(
                        title = agent.name,
                        body = agent.description.ifBlank { agent.systemBehavior },
                    )
                }
            }
        }
        item {
            LibrarySection(
                title = "Prompts",
                body = "Reusable templates with variables, preview data, archive/restore behavior, and versions.",
                actionLabel = "New prompt",
                onAction = onCreateStarterPrompt,
                isEmpty = libraryState.prompts.isEmpty(),
                emptyTitle = "No prompts yet",
                emptyBody = "Create a prompt template that agents or skills can reuse.",
            ) {
                libraryState.prompts.forEach { prompt ->
                    AssetRow(
                        title = prompt.title,
                        body = prompt.description.ifBlank { "Version ${prompt.currentVersion}" },
                    )
                }
            }
        }
        item {
            LibrarySection(
                title = "Skills",
                body = "Prompt-plus-tools bundles that agents and workflows can activate under explicit permissions.",
                actionLabel = "New skill",
                onAction = onCreateStarterSkill,
                isEmpty = libraryState.skills.isEmpty(),
                emptyTitle = "No skills yet",
                emptyBody = "Create a reusable skill bundle for a repeatable task.",
            ) {
                libraryState.skills.forEach { skill ->
                    AssetRow(
                        title = skill.title,
                        body = skill.description.ifBlank { "Version ${skill.currentVersion}" },
                    )
                }
            }
        }
        item {
            LibrarySection(
                title = "Tools",
                body = "Configurable built-in tools and declarative wrappers around approved app capabilities.",
                actionLabel = "New tool config",
                onAction = onCreateStarterToolConfig,
                isEmpty = libraryState.tools.isEmpty(),
                emptyTitle = "No tool configs yet",
                emptyBody = "Create a draft tool configuration to define permissions and approval behavior.",
            ) {
                libraryState.tools.forEach { tool ->
                    AssetRow(
                        title = tool.displayName,
                        body = "${tool.category} - ${if (tool.enabled) "Enabled" else "Disabled"}",
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkflowsFoundationScreen() {
    FoundationScreen(
        title = "Supervised autonomy",
        summary = "Workflows will run in WorkManager with progress, logs, retries, cancellation, and approval gates.",
        chips = listOf("WorkManager", "Approvals", "Logs", "Retries"),
        items = listOf(
            FoundationItem(
                title = "Visible progress",
                body = "Runs should be inspectable in chat and from the workflow detail screen.",
                icon = Icons.Outlined.Route,
            ),
            FoundationItem(
                title = "Side-effect safety",
                body = "Every side-effectful action pauses for confirmation before execution.",
                icon = Icons.Outlined.Hub,
            ),
        ),
    )
}

@Composable
private fun KnowledgeFoundationScreen() {
    FoundationScreen(
        title = "Private knowledge",
        summary = "Local RAG will import documents, extract text, chunk content, retrieve locally, and show citations.",
        chips = listOf("Local RAG", "Documents", "Embeddings", "Citations"),
        items = listOf(
            FoundationItem(
                title = "Collections",
                body = "Users will manage searchable knowledge collections with indexing status and storage details.",
                icon = Icons.Outlined.Route,
            ),
            FoundationItem(
                title = "Citations",
                body = "Agent answers using RAG must expose source snippets and retrieval decisions in debug traces.",
                icon = Icons.Outlined.AutoAwesome,
            ),
        ),
    )
}

@Composable
private fun ModelsFoundationScreen() {
    FoundationScreen(
        title = "Models and providers",
        summary = "Cloud keys, local model files, routing rules, capability badges, benchmarks, and budgets will live here.",
        chips = listOf("BYOK", "MediaPipe", "llama.cpp", "Budgets"),
        items = listOf(
            FoundationItem(
                title = "Provider adapters",
                body = "OpenAI, Anthropic, Gemini, and Mistral stream through one normalized generation event model.",
                icon = Icons.Outlined.Route,
            ),
            FoundationItem(
                title = "Local engines",
                body = "MediaPipe/LiteRT and llama.cpp adapters share a single local model engine contract.",
                icon = Icons.Outlined.Hub,
            ),
        ),
    )
}

@Composable
private fun DebugFoundationScreen() {
    FoundationScreen(
        title = "Inspectable runs",
        summary = "The debug console will make prompts, routing, tools, memory, RAG, voice, cost, and errors visible.",
        chips = listOf("Prompt", "Tools", "Memory", "RAG", "Voice", "Cost"),
        items = listOf(
            FoundationItem(
                title = "Trace-first runtime",
                body = "Agent runs should record enough context to understand behavior without storing secrets or raw audio.",
                icon = Icons.Outlined.Route,
            ),
            FoundationItem(
                title = "Replay path",
                body = "Eligible runs can later be replayed with the same inputs and selected model.",
                icon = Icons.Outlined.AutoAwesome,
            ),
        ),
    )
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
                title = "Settings foundation",
                summary = "Theme, dynamic color, privacy, cost, voice, retention, and safety controls will be managed from this screen.",
            )
        }
        item {
            SettingRow(
                title = "Dynamic color",
                body = "Use Android system colors when available. Explicit Lagents light and dark schemes remain the default.",
                checked = settings.dynamicColorEnabled,
                onCheckedChange = onDynamicColorChanged,
            )
        }
        item {
            SettingRow(
                title = "Local-only mode",
                body = "Block cloud calls, cloud fallback, and web tools when strict privacy is needed.",
                checked = settings.privacyMode == PrivacyMode.LocalOnly,
                onCheckedChange = onLocalOnlyModeChanged,
            )
        }
        item {
            SettingRow(
                title = "Sensitive text redaction",
                body = "Run a redaction pass before cloud calls where feasible and record the decision in debug traces.",
                checked = settings.sensitiveTextRedactionEnabled,
                onCheckedChange = onSensitiveTextRedactionChanged,
            )
        }
        item {
            SettingRow(
                title = "Confirm side effects",
                body = "Require explicit approval before tools perform external actions or modify app data.",
                checked = settings.requireApprovalForSideEffects,
                onCheckedChange = onRequireApprovalForSideEffectsChanged,
            )
        }
        item {
            ChoiceRow(
                title = "Routing preference",
                body = "Default model selection strategy for agents that do not override routing.",
                value = settings.routingPreference.displayName,
                onCycle = onCycleRoutingPreference,
            )
        }
        item {
            SettingRow(
                title = "Budget warnings",
                body = "Warn before expensive models, large contexts, or cloud fallback. Current monthly budget: ${settings.monthlyBudgetCents.toCurrencyLabel()}.",
                checked = settings.budgetWarningsEnabled,
                onCheckedChange = onBudgetWarningsChanged,
            )
        }
        item {
            ChoiceRow(
                title = "Voice input",
                body = "Default microphone behavior for chat composition.",
                value = settings.voiceInputMode.displayName,
                onCycle = onCycleVoiceInputMode,
            )
        }
        item {
            SettingRow(
                title = "Auto-read responses",
                body = "Read assistant messages aloud automatically when the active agent allows it.",
                checked = settings.autoReadAssistantResponses,
                onCheckedChange = onAutoReadAssistantResponsesChanged,
            )
        }
        item {
            ChoiceRow(
                title = "Transcript retention",
                body = "Controls whether speech transcript text is discarded or retained with conversations.",
                value = settings.transcriptRetention.displayName,
                onCycle = onCycleTranscriptRetention,
            )
        }
        items(
            listOf(
                FoundationItem(
                    title = "Privacy controls",
                    body = "Local-only mode, redaction, permissions, and retention controls are planned for this area.",
                    icon = Icons.Outlined.Mic,
                ),
                FoundationItem(
                    title = "Voice profiles",
                    body = "Language, voice, speech rate, pitch, transcript retention, and playback behavior will be configurable.",
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                ),
            ),
        ) { item ->
            FoundationCard(item = item)
        }
    }
}

@Composable
private fun FoundationScreen(
    title: String,
    summary: String,
    chips: List<String>,
    items: List<FoundationItem>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(title = title, summary = summary)
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEach { chip ->
                    AssistChip(
                        onClick = {},
                        label = { Text(text = chip) },
                    )
                }
            }
        }
        items(items) { item ->
            FoundationCard(item = item)
        }
        item {
            OutlinedButton(onClick = {}) {
                Text(text = "Implementation task list")
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    body: String,
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onCycle) {
                Text(text = value)
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    summary: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
    body: String,
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun AssetCountChip(
    label: String,
    count: Int,
) {
    FilterChip(
        selected = count > 0,
        onClick = {},
        label = {
            Text(text = "$label $count")
        },
    )
}

@Composable
private fun LibrarySection(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    isEmpty: Boolean,
    emptyTitle: String,
    emptyBody: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
            if (isEmpty) {
                LagentsEmptyState(
                    title = emptyTitle,
                    body = emptyBody,
                    actionLabel = actionLabel,
                    onAction = onAction,
                )
            } else {
                content()
            }
        }
    }
}

@Composable
private fun AssetRow(
    title: String,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class FoundationItem(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

private val Enum<*>.displayName: String
    get() = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")

private fun Int.toCurrencyLabel(): String = "$" + (this / 100)
