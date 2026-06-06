package com.msa.lagents.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.domain.model.GenerationEvent
import com.mikepenz.markdown.m3.Markdown

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
    modifier: Modifier = Modifier,
    isTwoPane: Boolean = false,
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(state.messages.size, state.streamingText) {
        if (state.messages.isNotEmpty() || state.streamingText.isNotEmpty()) {
            listState.animateScrollToItem((state.messages.size + (if (state.isGenerating) 1 else 0)).coerceAtLeast(0))
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
                            onClick = { /* TODO: selectConversation */ },
                            modifier = Modifier.padding(horizontal = 8.dp)
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

        // Input Bar
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
fun MessageBubble(
    message: MessageEntity,
    onTogglePlayback: (String) -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape
        ) {
            Column {
                if (isUser) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Markdown(
                        content = message.content,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (!isUser) {
                    IconButton(
                        onClick = { onTogglePlayback(message.content) },
                        modifier = Modifier.align(Alignment.End).padding(end = 4.dp, bottom = 4.dp).size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Speak",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = message.role.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun StreamingBubble(text: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Markdown(
                content = text,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = "Thinking...",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CitationsRow(citations: List<GenerationEvent.Citation>) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Sources",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            citations.forEach { citation ->
                AssistChip(
                    onClick = { /* TODO: Open document */ },
                    label = { Text(citation.title) },
                    leadingIcon = { Icon(Icons.Outlined.Source, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun MemorySuggestionCard(
    suggestion: GenerationEvent.MemorySuggestion,
    onAccept: () -> Unit,
    onIgnore: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Memory, null, tint = MaterialTheme.colorScheme.secondary)
                Text("New Memory Suggested", style = MaterialTheme.typography.titleSmall)
            }
            Text(suggestion.content, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onIgnore) { Text("Ignore") }
                Button(onClick = onAccept) { Text("Save Memory") }
            }
        }
    }
}

@Composable
fun WorkflowProgressCard(
    progress: GenerationEvent.WorkflowProgress,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(progress.status, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null)
                }
            }
            progress.progress?.let {
                LinearProgressIndicator(progress = { it }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ToolApprovalCard(
    approval: ToolApprovalState,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary)
                Text("Tool Approval Required", style = MaterialTheme.typography.titleSmall)
            }
            Text("The agent wants to use the tool: ${approval.toolName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Arguments: ${approval.arguments}", style = MaterialTheme.typography.bodySmall, maxLines = 5, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDeny) { Text("Deny") }
                Button(onClick = onApprove) { Text("Approve") }
            }
        }
    }
}
