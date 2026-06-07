package com.msa.lagents.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.local.manager.LocalModelManager
import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.data.provider.ProviderRepository
import com.msa.lagents.domain.local.LocalModelDescriptor
import com.msa.lagents.domain.local.LocalModelEngineType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModelsUiState(
    val localModels: List<LocalModelDescriptor> = emptyList(),
    val activeLocalModelId: String? = null,
    val cloudProviders: List<ProviderConfigEntity> = emptyList(),
)

class ModelsViewModel(
    private val localModelManager: LocalModelManager,
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    val uiState: StateFlow<ModelsUiState> = combine(
        localModelManager.models,
        localModelManager.activeModelId,
        providerRepository.providers
    ) { models, activeId, providers ->
        ModelsUiState(
            localModels = models,
            activeLocalModelId = activeId,
            cloudProviders = providers
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ModelsUiState()
    )

    fun addProvider(type: String, name: String, apiKey: String?, baseUrl: String? = null) {
        viewModelScope.launch {
            providerRepository.createProvider(type, name, apiKey, baseUrl)
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            providerRepository.deleteProvider(id)
        }
    }

    fun refreshLocalModels() {
        localModelManager.refreshModelStatus()
    }

    fun registerMockLocalModel() {
        // Kept for backward compatibility with UI button for now, but just refreshes
        refreshLocalModels()
    }

    fun loadLocalModel(id: String) {
        viewModelScope.launch {
            localModelManager.loadModel(id)
        }
    }

    fun unloadLocalModel() {
        viewModelScope.launch {
            localModelManager.unloadActiveModel()
        }
    }

    fun downloadLocalModel(id: String) {
        localModelManager.downloadModel(id)
    }

    fun deleteLocalModel(id: String) {
        localModelManager.deleteModel(id)
    }

    class Factory(
        private val localModelManager: LocalModelManager,
        private val providerRepository: ProviderRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModelsViewModel(localModelManager, providerRepository) as T
        }
    }
}
