package com.msa.lagents.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.data.settings.ThemePreference
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
