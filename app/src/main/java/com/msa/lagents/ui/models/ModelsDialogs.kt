package com.msa.lagents.ui.models

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.lagents.domain.model.ProviderType

@Composable
fun ProviderFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, name: String, apiKey: String, baseUrl: String?) -> Unit
) {
    var type by remember { mutableStateOf(ProviderType.OpenAI.name) }
    var name by remember { mutableStateOf("My OpenAI") }
    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }

    val providerTypes = ProviderType.entries.map { it.name }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Cloud Provider", style = MaterialTheme.typography.headlineSmall)

                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { },
                        label = { Text("Provider Type") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Text("▼") // Simple icon replacement
                            }
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        providerTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    name = "My $t"
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("sk-...") }
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://api.openai.com/v1") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            onConfirm(type, name, apiKey, baseUrl.takeIf { it.isNotBlank() })
                        },
                        enabled = apiKey.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
