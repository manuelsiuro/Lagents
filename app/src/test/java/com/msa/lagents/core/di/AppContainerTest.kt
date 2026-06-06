package com.msa.lagents.core.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppContainerTest {

    private lateinit var context: Context
    private lateinit var appContainer: AppContainer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        appContainer = AppContainer(context)
    }

    @Test
    fun `verify all dependencies are initialized`() {
        assertNotNull(appContainer.httpClient)
        assertNotNull(appContainer.database)
        assertNotNull(appContainer.appSettingsRepository)
        assertNotNull(appContainer.libraryRepository)
        assertNotNull(appContainer.appSettingsViewModelFactory)
        assertNotNull(appContainer.libraryViewModelFactory)
    }
}
