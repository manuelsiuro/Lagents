package com.msa.lagents.data.library

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.msa.lagents.data.local.LagentsDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LibraryRepositoryTest {

    private lateinit var db: LagentsDatabase
    private lateinit var repository: LibraryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LagentsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LibraryRepository(
            agentDao = db.agentDao(),
            promptDao = db.promptDao(),
            skillDao = db.skillDao(),
            toolConfigDao = db.toolConfigDao(),
            idGenerator = { "fixed-id" },
            nowMillis = { 1000L },
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `verify createStarterAgent`() = runTest {
        repository.createStarterAgent()
        val overview = repository.overview.first()
        assertEquals(1, overview.agents.size)
        assertEquals("fixed-id", overview.agents[0].id)
        assertEquals("New agent", overview.agents[0].name)
    }

    @Test
    fun `verify createStarterPrompt`() = runTest {
        repository.createStarterPrompt()
        val overview = repository.overview.first()
        assertEquals(1, overview.prompts.size)
        assertEquals("fixed-id", overview.prompts[0].id)
        assertEquals("New prompt", overview.prompts[0].title)
    }

    @Test
    fun `verify createStarterSkill`() = runTest {
        repository.createStarterSkill()
        val overview = repository.overview.first()
        assertEquals(1, overview.skills.size)
        assertEquals("fixed-id", overview.skills[0].id)
        assertEquals("New skill", overview.skills[0].title)
    }

    @Test
    fun `verify createStarterToolConfig`() = runTest {
        repository.createStarterToolConfig()
        val overview = repository.overview.first()
        assertEquals(1, overview.tools.size)
        assertEquals("fixed-id", overview.tools[0].id)
        assertEquals("New tool config", overview.tools[0].displayName)
    }
}
