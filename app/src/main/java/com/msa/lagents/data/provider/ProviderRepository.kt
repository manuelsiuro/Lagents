package com.msa.lagents.data.provider

import com.msa.lagents.data.local.provider.ProviderConfigDao
import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.domain.model.ModelDescriptor
import com.msa.lagents.domain.provider.ChatModelClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProviderRepository(
    private val providerConfigDao: ProviderConfigDao,
    private val clientFactory: ChatModelClientFactory,
    private val apiKeyStorage: ApiKeyStorage,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val providers: Flow<List<ProviderConfigEntity>> = providerConfigDao.observeProviderConfigs()

    private val _modelCache = MutableStateFlow<Map<String, List<ModelDescriptor>>>(emptyMap())
    val allModels: Flow<List<ModelDescriptor>> = combine(providers, _modelCache) { configs, cache ->
        configs.filter { it.enabled }.flatMap { cache[it.id] ?: emptyList() }
    }

    init {
        scope.launch {
            providers.collect { configs ->
                configs.filter { it.enabled }.forEach { config ->
                    refreshModels(config.id)
                }
            }
        }
    }

    suspend fun refreshModels(providerId: String) {
        val config = providerConfigDao.observeProviderConfig(providerId).first() ?: return
        val client = clientFactory.create(config) ?: return
        try {
            val models = client.getModels()
            _modelCache.value = _modelCache.value + (providerId to models)
        } catch (e: Exception) {
            // Log error or handle failure to fetch models
        }
    }

    fun getClient(providerId: String): ChatModelClient? {
        // This is a bit tricky as we might need to block or use a sync get.
        // For runtime, we usually have the config available.
        return null // Will implement a better way to get active clients
    }

    suspend fun createProvider(
        type: String,
        name: String,
        apiKey: String?,
        baseUrl: String? = null,
    ) {
        val id = idGenerator()
        val apiKeyAlias = apiKey?.let { 
            val alias = "key_${id.take(8)}"
            apiKeyStorage.storeApiKey(alias, it)
            alias
        }
        
        providerConfigDao.upsertProviderConfig(
            ProviderConfigEntity(
                id = id,
                providerType = type,
                displayName = name,
                enabled = true,
                apiKeyAlias = apiKeyAlias,
                baseUrl = baseUrl,
                defaultModelId = null,
                modelOverridesJson = "{}",
                budgetPolicyJson = "{}",
                createdAtMillis = nowMillis(),
                updatedAtMillis = nowMillis(),
            )
        )
    }

    suspend fun deleteProvider(id: String) {
        val config = providerConfigDao.observeProviderConfig(id).first()
        config?.apiKeyAlias?.let { apiKeyStorage.deleteApiKey(it) }
        providerConfigDao.deleteProviderConfig(id)
        _modelCache.value = _modelCache.value - id
    }
}
