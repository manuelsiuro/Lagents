package com.msa.lagents.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msa.lagents.core.di.AppContainer
import com.msa.lagents.ui.library.LibraryViewModel
import com.msa.lagents.ui.navigation.LagentsNavScaffold
import com.msa.lagents.ui.settings.AppSettingsViewModel
import com.msa.lagents.ui.theme.LagentsTheme

@Composable
fun LagentsApp(appContainer: AppContainer) {
    val settingsViewModel: AppSettingsViewModel = viewModel(
        factory = appContainer.appSettingsViewModelFactory,
    )
    val settings by settingsViewModel.uiState.collectAsState()
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = appContainer.libraryViewModelFactory,
    )
    val libraryState by libraryViewModel.uiState.collectAsState()

    LagentsTheme(
        themePreference = settings.themePreference,
        dynamicColor = settings.dynamicColorEnabled,
    ) {
        LagentsNavScaffold(
            settings = settings,
            libraryState = libraryState,
            onCycleTheme = settingsViewModel::cycleThemePreference,
            onDynamicColorChanged = settingsViewModel::setDynamicColorEnabled,
            onLocalOnlyModeChanged = settingsViewModel::toggleLocalOnlyMode,
            onSensitiveTextRedactionChanged = settingsViewModel::setSensitiveTextRedactionEnabled,
            onRequireApprovalForSideEffectsChanged = settingsViewModel::setRequireApprovalForSideEffects,
            onCycleRoutingPreference = settingsViewModel::cycleRoutingPreference,
            onBudgetWarningsChanged = settingsViewModel::setBudgetWarningsEnabled,
            onCycleVoiceInputMode = settingsViewModel::cycleVoiceInputMode,
            onAutoReadAssistantResponsesChanged = settingsViewModel::setAutoReadAssistantResponses,
            onCycleTranscriptRetention = settingsViewModel::cycleTranscriptRetention,
            onCreateStarterAgent = libraryViewModel::createStarterAgent,
            onCreateStarterPrompt = libraryViewModel::createStarterPrompt,
            onCreateStarterSkill = libraryViewModel::createStarterSkill,
            onCreateStarterToolConfig = libraryViewModel::createStarterToolConfig,
        )
    }
}
