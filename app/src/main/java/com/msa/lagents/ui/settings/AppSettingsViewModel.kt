package com.msa.lagents.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.data.settings.PrivacyMode
import com.msa.lagents.data.settings.RoutingPreference
import com.msa.lagents.data.settings.ThemePreference
import com.msa.lagents.data.settings.TranscriptRetention
import com.msa.lagents.data.settings.VoiceInputMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<AppSettings> = appSettingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AppSettings(),
    )

    fun cycleThemePreference() {
        val nextPreference = when (uiState.value.themePreference) {
            ThemePreference.System -> ThemePreference.Light
            ThemePreference.Light -> ThemePreference.Dark
            ThemePreference.Dark -> ThemePreference.System
        }
        viewModelScope.launch {
            appSettingsRepository.setThemePreference(nextPreference)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setDynamicColorEnabled(enabled)
        }
    }

    fun toggleLocalOnlyMode(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setPrivacyMode(
                if (enabled) PrivacyMode.LocalOnly else PrivacyMode.Standard,
            )
        }
    }

    fun setSensitiveTextRedactionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setSensitiveTextRedactionEnabled(enabled)
        }
    }

    fun setRequireApprovalForSideEffects(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setRequireApprovalForSideEffects(enabled)
        }
    }

    fun cycleRoutingPreference() {
        val nextPreference = uiState.value.routingPreference.next()
        viewModelScope.launch {
            appSettingsRepository.setRoutingPreference(nextPreference)
        }
    }

    fun setBudgetWarningsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setBudgetWarningsEnabled(enabled)
        }
    }

    fun setMonthlyBudgetCents(monthlyBudgetCents: Int) {
        viewModelScope.launch {
            appSettingsRepository.setMonthlyBudgetCents(monthlyBudgetCents)
        }
    }

    fun cycleVoiceInputMode() {
        val nextMode = uiState.value.voiceInputMode.next()
        viewModelScope.launch {
            appSettingsRepository.setVoiceInputMode(nextMode)
        }
    }

    fun setAutoReadAssistantResponses(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setAutoReadAssistantResponses(enabled)
        }
    }

    fun cycleTranscriptRetention() {
        val nextRetention = uiState.value.transcriptRetention.next()
        viewModelScope.launch {
            appSettingsRepository.setTranscriptRetention(nextRetention)
        }
    }

    class Factory(
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
                return AppSettingsViewModel(appSettingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun RoutingPreference.next(): RoutingPreference = when (this) {
    RoutingPreference.Balanced -> RoutingPreference.PrivacyFirst
    RoutingPreference.PrivacyFirst -> RoutingPreference.CostFirst
    RoutingPreference.CostFirst -> RoutingPreference.CapabilityFirst
    RoutingPreference.CapabilityFirst -> RoutingPreference.Balanced
}

private fun VoiceInputMode.next(): VoiceInputMode = when (this) {
    VoiceInputMode.PushToTalk -> VoiceInputMode.ContinuousDictation
    VoiceInputMode.ContinuousDictation -> VoiceInputMode.PushToTalk
}

private fun TranscriptRetention.next(): TranscriptRetention = when (this) {
    TranscriptRetention.Never -> TranscriptRetention.SessionOnly
    TranscriptRetention.SessionOnly -> TranscriptRetention.ConversationHistory
    TranscriptRetention.ConversationHistory -> TranscriptRetention.Never
}
