package com.msa.lagents.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val APP_SETTINGS_DATASTORE_NAME = "app_settings"

private val Context.appSettingsDataStore by preferencesDataStore(
    name = APP_SETTINGS_DATASTORE_NAME,
)

class AppSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.appSettingsDataStore)

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themePreference = preferences[THEME_PREFERENCE_KEY]
                ?.let { enumFromStorage<ThemePreference>(it) }
                ?: ThemePreference.System,
            dynamicColorEnabled = preferences[DYNAMIC_COLOR_ENABLED_KEY] ?: false,
            privacyMode = preferences[PRIVACY_MODE_KEY]
                ?.let { enumFromStorage<PrivacyMode>(it) }
                ?: PrivacyMode.Standard,
            sensitiveTextRedactionEnabled = preferences[SENSITIVE_TEXT_REDACTION_ENABLED_KEY] ?: true,
            requireApprovalForSideEffects = preferences[REQUIRE_APPROVAL_FOR_SIDE_EFFECTS_KEY] ?: true,
            routingPreference = preferences[ROUTING_PREFERENCE_KEY]
                ?.let { enumFromStorage<RoutingPreference>(it) }
                ?: RoutingPreference.Balanced,
            budgetWarningsEnabled = preferences[BUDGET_WARNINGS_ENABLED_KEY] ?: true,
            monthlyBudgetCents = preferences[MONTHLY_BUDGET_CENTS_KEY] ?: 5_000,
            voiceInputMode = preferences[VOICE_INPUT_MODE_KEY]
                ?.let { enumFromStorage<VoiceInputMode>(it) }
                ?: VoiceInputMode.PushToTalk,
            autoReadAssistantResponses = preferences[AUTO_READ_ASSISTANT_RESPONSES_KEY] ?: false,
            transcriptRetention = preferences[TRANSCRIPT_RETENTION_KEY]
                ?.let { enumFromStorage<TranscriptRetention>(it) }
                ?: TranscriptRetention.SessionOnly,
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

    suspend fun setPrivacyMode(privacyMode: PrivacyMode) {
        dataStore.edit { preferences ->
            preferences[PRIVACY_MODE_KEY] = privacyMode.name
        }
    }

    suspend fun setSensitiveTextRedactionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SENSITIVE_TEXT_REDACTION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setRequireApprovalForSideEffects(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REQUIRE_APPROVAL_FOR_SIDE_EFFECTS_KEY] = enabled
        }
    }

    suspend fun setRoutingPreference(routingPreference: RoutingPreference) {
        dataStore.edit { preferences ->
            preferences[ROUTING_PREFERENCE_KEY] = routingPreference.name
        }
    }

    suspend fun setBudgetWarningsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BUDGET_WARNINGS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setMonthlyBudgetCents(monthlyBudgetCents: Int) {
        dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET_CENTS_KEY] = monthlyBudgetCents.coerceAtLeast(0)
        }
    }

    suspend fun setVoiceInputMode(voiceInputMode: VoiceInputMode) {
        dataStore.edit { preferences ->
            preferences[VOICE_INPUT_MODE_KEY] = voiceInputMode.name
        }
    }

    suspend fun setAutoReadAssistantResponses(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_READ_ASSISTANT_RESPONSES_KEY] = enabled
        }
    }

    suspend fun setTranscriptRetention(transcriptRetention: TranscriptRetention) {
        dataStore.edit { preferences ->
            preferences[TRANSCRIPT_RETENTION_KEY] = transcriptRetention.name
        }
    }

    private inline fun <reified T : Enum<T>> enumFromStorage(value: String): T? =
        enumValues<T>().firstOrNull { it.name == value }

    private companion object {
        val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
        val DYNAMIC_COLOR_ENABLED_KEY = booleanPreferencesKey("dynamic_color_enabled")
        val PRIVACY_MODE_KEY = stringPreferencesKey("privacy_mode")
        val SENSITIVE_TEXT_REDACTION_ENABLED_KEY = booleanPreferencesKey("sensitive_text_redaction_enabled")
        val REQUIRE_APPROVAL_FOR_SIDE_EFFECTS_KEY = booleanPreferencesKey("require_approval_for_side_effects")
        val ROUTING_PREFERENCE_KEY = stringPreferencesKey("routing_preference")
        val BUDGET_WARNINGS_ENABLED_KEY = booleanPreferencesKey("budget_warnings_enabled")
        val MONTHLY_BUDGET_CENTS_KEY = intPreferencesKey("monthly_budget_cents")
        val VOICE_INPUT_MODE_KEY = stringPreferencesKey("voice_input_mode")
        val AUTO_READ_ASSISTANT_RESPONSES_KEY = booleanPreferencesKey("auto_read_assistant_responses")
        val TRANSCRIPT_RETENTION_KEY = stringPreferencesKey("transcript_retention")
    }
}
