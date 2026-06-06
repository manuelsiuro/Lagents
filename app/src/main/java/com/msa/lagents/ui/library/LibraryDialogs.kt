package com.msa.lagents.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.tool.ToolConfigEntity

@Composable
fun AgentFormDialog(
    agent: AgentEntity,
    onDismiss: () -> Unit,
    onConfirm: (AgentEntity) -> Unit,
) {
    var name by remember { mutableStateOf(agent.name) }
    var description by remember { mutableStateOf(agent.description) }
    var systemBehavior by remember { mutableStateOf(agent.systemBehavior) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Agent") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = systemBehavior,
                    onValueChange = { systemBehavior = it },
                    label = { Text("System Behavior") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(agent.copy(name = name, description = description, systemBehavior = systemBehavior))
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun PromptFormDialog(
    prompt: PromptEntity,
    onDismiss: () -> Unit,
    onConfirm: (PromptEntity) -> Unit,
) {
    var title by remember { mutableStateOf(prompt.title) }
    var description by remember { mutableStateOf(prompt.description) }
    var systemText by remember { mutableStateOf(prompt.systemText) }
    var userText by remember { mutableStateOf(prompt.userText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Prompt") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = systemText,
                    onValueChange = { systemText = it },
                    label = { Text("System Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = userText,
                    onValueChange = { userText = it },
                    label = { Text("User Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(prompt.copy(title = title, description = description, systemText = systemText, userText = userText))
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun SkillFormDialog(
    skill: SkillEntity,
    onDismiss: () -> Unit,
    onConfirm: (SkillEntity) -> Unit,
) {
    var title by remember { mutableStateOf(skill.title) }
    var description by remember { mutableStateOf(skill.description) }
    var instructions by remember { mutableStateOf(skill.instructions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Skill") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(skill.copy(title = title, description = description, instructions = instructions))
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun ToolConfigFormDialog(
    toolConfig: ToolConfigEntity,
    onDismiss: () -> Unit,
    onConfirm: (ToolConfigEntity) -> Unit,
) {
    var displayName by remember { mutableStateOf(toolConfig.displayName) }
    var description by remember { mutableStateOf(toolConfig.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tool Config") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(toolConfig.copy(displayName = displayName, description = description))
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
