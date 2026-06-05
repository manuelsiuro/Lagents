package com.msa.lagents.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class LagentsDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val summary: String,
) {
    Chat(
        route = "chat",
        label = "Chat",
        icon = Icons.AutoMirrored.Outlined.Chat,
        summary = "Agent chat with tools, memory, voice, and model routing.",
    ),
    Library(
        route = "library",
        label = "Library",
        icon = Icons.AutoMirrored.Outlined.LibraryBooks,
        summary = "Agents, prompts, skills, and tool configurations.",
    ),
    Workflows(
        route = "workflows",
        label = "Workflows",
        icon = Icons.Outlined.Work,
        summary = "Supervised autonomous runs with approvals and logs.",
    ),
    Knowledge(
        route = "knowledge",
        label = "Knowledge",
        icon = Icons.Outlined.Folder,
        summary = "Private local document collections and retrieval.",
    ),
    Models(
        route = "models",
        label = "Models",
        icon = Icons.Outlined.Memory,
        summary = "Cloud providers, local models, benchmarks, and budgets.",
    ),
    Debug(
        route = "debug",
        label = "Debug",
        icon = Icons.Outlined.BugReport,
        summary = "Prompt, routing, tool, memory, RAG, voice, and cost traces.",
    ),
    Settings(
        route = "settings",
        label = "Settings",
        icon = Icons.Outlined.Settings,
        summary = "Privacy, theme, voice, retention, and safety controls.",
    ),
}

val topLevelDestinations: List<LagentsDestination> = LagentsDestination.entries
