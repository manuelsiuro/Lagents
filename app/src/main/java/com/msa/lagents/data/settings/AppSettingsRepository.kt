package com.msa.lagents.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val APP_SETTINGS_DATASTORE_NAME = "app_settings"

private val Context.appSettingsDataStore by preferencesDataStore(
    name = APP_SETTINGS_DATASTORE_NAME,
)

class AppSettingsRepository(context: Context) {
    private val dataStore = context.appSettingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themePreference = preferences[THEME_PREFERENCE_KEY]
                ?.let(::themePreferenceFromStorage)
                ?: ThemePreference.System,
            dynamicColorEnabled = preferences[DYNAMIC_COLOR_ENABLED_KEY] ?: false,
        )
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = themePreference.name
        }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_ENABLED_KEY] = enabled
        }
    }

    private fun themePreferenceFromStorage(value: String): ThemePreference =
        ThemePreference.entries.firstOrNull { it.name == value } ?: ThemePreference.System

    private companion object {
        val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
        val DYNAMIC_COLOR_ENABLED_KEY = booleanPreferencesKey("dynamic_color_enabled")
    }
}
