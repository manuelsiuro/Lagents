package com.msa.lagents.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.local.manager.LocalModelManager
import com.msa.lagents.domain.local.LocalModelDescriptor
import com.msa.lagents.domain.local.LocalModelEngineType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocalModelManagerState(
    val models: List<LocalModelDescriptor> = emptyList(),
    val activeModelId: String? = null,
)

class LocalModelManagerViewModel(
    private val localModelManager: LocalModelManager,
) : ViewModel() {

    val uiState: StateFlow<LocalModelManagerState> = kotlinx.coroutines.flow.combine(
        localModelManager.models,
        localModelManager.activeModelId
    ) { models, activeId ->
        LocalModelManagerState(models = models, activeModelId = activeId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocalModelManagerState()
    )

    fun registerMockModel() {
        viewModelScope.launch {
            localModelManager.registerModel(
                LocalModelDescriptor(
                    id = "gemma-2b",
                    path = "/sdcard/Download/gemma-2b-it-gpu-int4.bin",
                    engine = LocalModelEngineType.MediaPipe,
                    displayName = "Gemma 2B (IT)",
                    sizeBytes = 1_500_000_000,
                    contextWindowTokens = 2048
                )
            )
        }
    }

    fun loadModel(id: String) {
        viewModelScope.launch {
            localModelManager.loadModel(id)
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            localModelManager.unloadActiveModel()
        }
    }

    class Factory(private val localModelManager: LocalModelManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocalModelManagerViewModel(localModelManager) as T
        }
    }
}
