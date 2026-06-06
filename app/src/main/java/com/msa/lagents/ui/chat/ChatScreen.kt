package com.msa.lagents.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.voice.SpeechPlaybackState

@Composable
fun ChatScreen(
    state: ChatUiState,
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
    modifier: Modifier = Modifier,
    isTwoPane: Boolean = false,
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size, state.streamingText) {
        if (state.messages.isNotEmpty() || state.streamingText.isNotEmpty()) {
            listState.animateScrollToItem((state.messages.size + 1).coerceAtLeast(0))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            if (isTwoPane) {
                // Conversation List
                LazyColumn(
                    modifier = Modifier.width(300.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    item {
                        Text(
                            "Chats",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(state.conversations) { conv ->
                        NavigationDrawerItem(
                            label = { Text(conv.title) },
                            selected = conv.id == state.currentConversationId,
                            onClick = { onSelectConversation(conv.id) },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            badge = {
                                IconButton(onClick = { onDeleteConversation(conv.id) }) {
                                    Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
                VerticalDivider()
            }

            Column(modifier = Modifier.weight(1f)) {
                // Agent Picker
                if (state.agents.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = state.agents.indexOfFirst { it.id == state.selectedAgentId }.coerceAtLeast(0),
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        state.agents.forEach { agent ->
                            Tab(
                                selected = agent.id == state.selectedAgentId,
                                onClick = { onSelectAgent(agent.id) },
                                text = { Text(agent.name) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    // Message List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.messages) { message ->
                            MessageBubble(
                                message = message,
                                onTogglePlayback = onTogglePlayback
                            )
                        }
                        if (state.isGenerating && state.streamingText.isNotEmpty()) {
                            item {
                                StreamingBubble(state.streamingText)
                            }
                        }
                        state.pendingApproval?.let { approval ->
                            item {
                                ToolApprovalCard(
                                    approval = approval,
                                    onApprove = { onApproveTool(true) },
                                    onDeny = { onApproveTool(false) }
                                )
                            }
                        }
                        if (state.citations.isNotEmpty()) {
                            item {
                                CitationsRow(state.citations)
                            }
                        }
                        state.memorySuggestions.forEach { suggestion ->
                            item {
                                MemorySuggestionCard(
                                    suggestion = suggestion,
                                    onAccept = { onAcceptMemory(suggestion.id, true) },
                                    onIgnore = { onAcceptMemory(suggestion.id, false) }
                                )
                            }
                        }
                        state.workflowProgress?.let { progress ->
                            item {
                                WorkflowProgressCard(
                                    progress = progress,
                                    onDismiss = onDismissWorkflow
                                )
                            }
                        }
                    }

                    if (state.isRecording && state.voiceTranscript != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.BottomCenter)
                                .offset(y = (-16).dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Listening...", style = MaterialTheme.typography.labelSmall)
                                Text(state.voiceTranscript, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask your agent...") },
                    maxLines = 4
                )
                
                IconButton(
                    onClick = { if (state.isRecording) onStopVoice() else onStartVoice() },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (state.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (state.isRecording) "Stop recording" else "Start voice input"
                    )
                }

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    enabled = inputText.isNotBlank() && !state.isGenerating,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onTogglePlayback: (String) -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = MaterialTheme.shapes.medium

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            tonalElevation = if (isUser) 0.dp else 1.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!isUser) {
                    IconButton(
                        onClick = { onTogglePlayback(message.id) },
                        modifier = Modifier.align(Alignment.End).size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Speak", modifier = Modifier.size(16.dp)) // Using Send as placeholder for Speak
                    }
                }
            }
        }
        Text(
            text = if (isUser) "You" else "Assistant",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun StreamingBubble(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        LinearProgressIndicator(
            modifier = Modifier
                .width(100.dp)
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.small)
        )
    }
}

@Composable
private fun CitationsRow(citations: List<GenerationEvent.Citation>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Sources", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            citations.forEach { citation ->
                AssistChip(
                    onClick = { /* TODO: Open source */ },
                    label = { Text(citation.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@Composable
private fun MemorySuggestionCard(
    suggestion: GenerationEvent.MemorySuggestion,
    onAccept: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Remember this?", style = MaterialTheme.typography.titleSmall)
            Text(suggestion.content, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onIgnore) { Text("Ignore") }
                Button(onClick = onAccept) { Text("Save Memory") }
            }
        }
    }
}

@Composable
private fun WorkflowProgressCard(
    progress: GenerationEvent.WorkflowProgress,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Workflow: Progress", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f)) // Simplified
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Stop, null)
                }
            }
            val p = progress.progress
            if (p != null) {
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                )
            }
            Text(progress.status, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ToolApprovalCard(
    approval: ToolApprovalState,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tool Approval Required", style = MaterialTheme.typography.titleSmall)
            Text("The agent wants to use: ${approval.toolName}", style = MaterialTheme.typography.bodyMedium)
            Text(approval.arguments, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDeny) { Text("Deny") }
                Button(onClick = onApprove) { Text("Approve") }
            }
        }
    }
}
