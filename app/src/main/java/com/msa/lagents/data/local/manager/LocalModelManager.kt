package com.msa.lagents.data.local.manager

import com.msa.lagents.domain.local.LocalModelDescriptor
import com.msa.lagents.domain.local.LocalModelEngine
import com.msa.lagents.domain.local.LocalModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalModelManager(
    private val engine: LocalModelEngine,
) {
    private val _models = MutableStateFlow<List<LocalModelDescriptor>>(emptyList())
    val models: StateFlow<List<LocalModelDescriptor>> = _models.asStateFlow()

    private val _activeModelId = MutableStateFlow<String?>(null)
    val activeModelId: StateFlow<String?> = _activeModelId.asStateFlow()

    suspend fun registerModel(model: LocalModelDescriptor) {
        _models.value = _models.value + model
    }

    suspend fun loadModel(id: String) {
        val model = _models.value.find { it.id == id } ?: return
        
        _models.value = _models.value.map { 
            if (it.id == id) it.copy(status = LocalModelStatus.Loading) else it 
        }

        val result = engine.load(model)
        
        _models.value = _models.value.map { 
            if (it.id == id) {
                it.copy(status = if (result.isSuccess) LocalModelStatus.Ready else LocalModelStatus.Error)
            } else {
                it.copy(status = LocalModelStatus.NotLoaded) // Ensure others are unloaded
            }
        }

        if (result.isSuccess) {
            _activeModelId.value = id
        }
    }

    suspend fun unloadActiveModel() {
        engine.unload()
        _activeModelId.value = null
        _models.value = _models.value.map { it.copy(status = LocalModelStatus.NotLoaded) }
    }
}
