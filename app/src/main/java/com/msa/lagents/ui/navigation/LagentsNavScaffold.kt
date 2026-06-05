package com.msa.lagents.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.ThemePreference
import com.msa.lagents.ui.screens.DestinationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LagentsNavScaffold(
    settings: AppSettings,
    onCycleTheme: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedDestination = topLevelDestinations.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    } ?: LagentsDestination.Chat

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 840.dp

            if (useRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    LagentsNavigationRail(
                        selectedDestination = selectedDestination,
                        onDestinationSelected = { destination ->
                            navController.navigateTopLevel(destination)
                        },
                    )
                    Scaffold(
                        topBar = {
                            LagentsTopBar(
                                destination = selectedDestination,
                                themePreference = settings.themePreference,
                                onCycleTheme = onCycleTheme,
                            )
                        },
                    ) { innerPadding ->
                        LagentsNavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            settings = settings,
                            onDynamicColorChanged = onDynamicColorChanged,
                        )
                    }
                }
            } else {
                Scaffold(
                    topBar = {
                        LagentsTopBar(
                            destination = selectedDestination,
                            themePreference = settings.themePreference,
                            onCycleTheme = onCycleTheme,
                        )
                    },
                    bottomBar = {
                        LagentsNavigationBar(
                            selectedDestination = selectedDestination,
                            onDestinationSelected = { destination ->
                                navController.navigateTopLevel(destination)
                            },
                        )
                    },
                ) { innerPadding ->
                    LagentsNavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        settings = settings,
                        onDynamicColorChanged = onDynamicColorChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun LagentsNavHost(
    modifier: Modifier,
    navController: NavHostController,
    settings: AppSettings,
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = LagentsDestination.Chat.route,
        ) {
            topLevelDestinations.forEach { destination ->
                composable(destination.route) {
                    DestinationScreen(
                        destination = destination,
                        settings = settings,
                        onDynamicColorChanged = onDynamicColorChanged,
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
    CenterAlignedTopAppBar(
        title = {
            Text(text = destination.label)
        },
        actions = {
            IconButton(onClick = onCycleTheme) {
                val icon = when (themePreference) {
                    ThemePreference.Dark -> Icons.Outlined.DarkMode
                    ThemePreference.Light -> Icons.Outlined.LightMode
                    ThemePreference.System -> Icons.Outlined.Storage
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Change theme",
                )
            }
        },
    )
}

@Composable
private fun LagentsNavigationBar(
    selectedDestination: LagentsDestination,
    onDestinationSelected: (LagentsDestination) -> Unit,
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val primaryDestinations = listOf(
        LagentsDestination.Chat,
        LagentsDestination.Library,
        LagentsDestination.Workflows,
        LagentsDestination.Models,
    )
    val overflowDestinations = listOf(
        LagentsDestination.Knowledge,
        LagentsDestination.Debug,
        LagentsDestination.Settings,
    )

    NavigationBar {
        primaryDestinations.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(text = destination.label)
                },
            )
        }
        NavigationBarItem(
            selected = selectedDestination in overflowDestinations,
            onClick = { showMoreMenu = true },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "More destinations",
                )
            },
            label = {
                Text(text = "More")
            },
        )
        DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false },
        ) {
            overflowDestinations.forEach { destination ->
                DropdownMenuItem(
                    text = { Text(text = destination.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onDestinationSelected(destination)
                    },
                )
            }
        }
    }
}

@Composable
private fun LagentsNavigationRail(
    selectedDestination: LagentsDestination,
    onDestinationSelected: (LagentsDestination) -> Unit,
) {
    NavigationRail {
        topLevelDestinations.forEach { destination ->
            NavigationRailItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(text = destination.label)
                },
            )
        }
    }
}

private fun androidx.navigation.NavHostController.navigateTopLevel(
    destination: LagentsDestination,
) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
