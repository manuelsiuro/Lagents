package com.msa.lagents.data.provider

import com.msa.lagents.data.local.provider.ProviderConfigDao
import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProviderRepositoryTest {

    private val providerConfigDao: ProviderConfigDao = mock()
    private val clientFactory: ChatModelClientFactory = mock()
    private val apiKeyStorage: ApiKeyStorage = mock()
    private val testScope = TestScope()

    private lateinit var repository: ProviderRepository

    @Before
    fun setUp() {
        whenever(providerConfigDao.observeProviderConfigs()).thenReturn(flowOf(emptyList()))
        repository = ProviderRepository(
            providerConfigDao = providerConfigDao,
            clientFactory = clientFactory,
            apiKeyStorage = apiKeyStorage,
            scope = testScope,
            idGenerator = { "test-id" },
            nowMillis = { 1000L }
        )
    }

    @Test
    fun `createProvider calls DAO and storage`() = runTest {
        repository.createProvider("openai", "OpenAI", "sk-123")
        
        org.mockito.kotlin.verify(apiKeyStorage).storeApiKey(org.mockito.kotlin.eq("key_test-id"), org.mockito.kotlin.eq("sk-123"))
        org.mockito.kotlin.verify(providerConfigDao).upsertProviderConfig(any())
    }
}
