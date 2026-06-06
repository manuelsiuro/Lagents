package com.msa.lagents.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class LagentsDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val summary: String,
) {
    object Chat : LagentsDestination(
        route = "chat",
        label = "Chat",
        icon = Icons.Outlined.Chat,
        summary = "Talk to your agents"
    )

    object Library : LagentsDestination(
        route = "library",
        label = "Library",
        icon = Icons.Outlined.LibraryBooks,
        summary = "Manage agents and skills"
    )

    object Workflows : LagentsDestination(
        route = "workflows",
        label = "Workflows",
        icon = Icons.Outlined.Router,
        summary = "Autonomous automation"
    )

    object Knowledge : LagentsDestination(
        route = "knowledge",
        label = "Knowledge",
        icon = Icons.Outlined.Memory,
        summary = "Private local RAG"
    )

    object Models : LagentsDestination(
        route = "models",
        label = "Models",
        icon = Icons.Outlined.Hub,
        summary = "Providers and local LLMs"
    )

    object Debug : LagentsDestination(
        route = "debug",
        label = "Debug",
        icon = Icons.Outlined.Terminal,
        summary = "Audit traces and logs"
    )

    object Settings : LagentsDestination(
        route = "settings",
        label = "Settings",
        icon = Icons.Outlined.Settings,
        summary = "Global app configuration"
    )
}

val topLevelDestinations = listOf(
    LagentsDestination.Chat,
    LagentsDestination.Library,
    LagentsDestination.Workflows,
    LagentsDestination.Knowledge,
    LagentsDestination.Models,
    LagentsDestination.Debug,
    LagentsDestination.Settings,
)
