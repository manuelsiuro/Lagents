package com.msa.lagents.data.provider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApiKeyStorageTest {

    private lateinit var storage: ApiKeyStorage

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        storage = ApiKeyStorage(context)
    }

    @Test
    fun `store and get api key`() {
        storage.storeApiKey("test-alias", "sk-12345")
        assertEquals("sk-12345", storage.getApiKey("test-alias"))
    }

    @Test
    fun `delete api key`() {
        storage.storeApiKey("test-alias", "sk-12345")
        storage.deleteApiKey("test-alias")
        assertNull(storage.getApiKey("test-alias"))
    }

    @Test
    fun `clear all api keys`() {
        storage.storeApiKey("alias-1", "key-1")
        storage.storeApiKey("alias-2", "key-2")
        storage.clearAll()
        assertNull(storage.getApiKey("alias-1"))
        assertNull(storage.getApiKey("alias-2"))
    }
}
