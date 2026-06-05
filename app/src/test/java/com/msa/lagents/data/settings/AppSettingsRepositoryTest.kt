package com.msa.lagents.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun settingsExposeDefaults() = runTest {
        val repository = createRepository()

        val settings = repository.settings.first()

        assertEquals(ThemePreference.System, settings.themePreference)
        assertEquals(false, settings.dynamicColorEnabled)
        assertEquals(PrivacyMode.Standard, settings.privacyMode)
        assertEquals(true, settings.sensitiveTextRedactionEnabled)
        assertEquals(true, settings.requireApprovalForSideEffects)
        assertEquals(RoutingPreference.Balanced, settings.routingPreference)
        assertEquals(true, settings.budgetWarningsEnabled)
        assertEquals(5_000, settings.monthlyBudgetCents)
        assertEquals(VoiceInputMode.PushToTalk, settings.voiceInputMode)
        assertEquals(false, settings.autoReadAssistantResponses)
        assertEquals(TranscriptRetention.SessionOnly, settings.transcriptRetention)
    }

    @Test
    fun settingsPersistUpdates() = runTest {
        val repository = createRepository()

        repository.setThemePreference(ThemePreference.Dark)
        repository.setDynamicColorEnabled(true)
        repository.setPrivacyMode(PrivacyMode.LocalOnly)
        repository.setSensitiveTextRedactionEnabled(false)
        repository.setRequireApprovalForSideEffects(false)
        repository.setRoutingPreference(RoutingPreference.PrivacyFirst)
        repository.setBudgetWarningsEnabled(false)
        repository.setMonthlyBudgetCents(12_500)
        repository.setVoiceInputMode(VoiceInputMode.ContinuousDictation)
        repository.setAutoReadAssistantResponses(true)
        repository.setTranscriptRetention(TranscriptRetention.ConversationHistory)

        val settings = repository.settings.first()

        assertEquals(ThemePreference.Dark, settings.themePreference)
        assertEquals(true, settings.dynamicColorEnabled)
        assertEquals(PrivacyMode.LocalOnly, settings.privacyMode)
        assertEquals(false, settings.sensitiveTextRedactionEnabled)
        assertEquals(false, settings.requireApprovalForSideEffects)
        assertEquals(RoutingPreference.PrivacyFirst, settings.routingPreference)
        assertEquals(false, settings.budgetWarningsEnabled)
        assertEquals(12_500, settings.monthlyBudgetCents)
        assertEquals(VoiceInputMode.ContinuousDictation, settings.voiceInputMode)
        assertEquals(true, settings.autoReadAssistantResponses)
        assertEquals(TranscriptRetention.ConversationHistory, settings.transcriptRetention)
    }

    private fun TestScope.createRepository(): AppSettingsRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher(testScheduler) + Job()),
            produceFile = {
                temporaryFolder.newFile("settings.preferences_pb")
            },
        )
        return AppSettingsRepository(dataStore)
    }
}
