package com.msa.lagents.data.local.manager

import android.content.Context
import com.msa.lagents.domain.local.LocalModelDescriptor
import com.msa.lagents.domain.local.LocalModelEngine
import com.msa.lagents.domain.local.LocalModelEngineType
import com.msa.lagents.domain.local.LocalModelStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LocalModelManager(
    private val context: Context,
    private val engine: LocalModelEngine,
    private val downloader: LocalModelDownloader
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val curatedModels = listOf(
        LocalModelDescriptor(
            id = "gemma-2b-it-cpu",
            path = "",
            engine = LocalModelEngineType.MediaPipe,
            displayName = "Gemma 2B IT (CPU)",
            sizeBytes = 1_350_000_000,
            contextWindowTokens = 2048,
            downloadUrl = "https://huggingface.co/google/gemma-2b-it-tflite/resolve/main/gemma-2b-it-cpu-int4.bin"
        ),
        LocalModelDescriptor(
            id = "gemma-2b-it-gpu",
            path = "",
            engine = LocalModelEngineType.MediaPipe,
            displayName = "Gemma 2B IT (GPU)",
            sizeBytes = 1_350_000_000,
            contextWindowTokens = 2048,
            downloadUrl = "https://huggingface.co/google/gemma-2b-it-tflite/resolve/main/gemma-2b-it-gpu-int4.bin"
        )
    )

    private val _models = MutableStateFlow<List<LocalModelDescriptor>>(curatedModels)
    val models: StateFlow<List<LocalModelDescriptor>> = _models.asStateFlow()

    private val _activeModelId = MutableStateFlow<String?>(null)
    val activeModelId: StateFlow<String?> = _activeModelId.asStateFlow()

    init {
        refreshModelStatus()
    }

    fun refreshModelStatus() {
        val updatedModels = _models.value.map { model ->
            val file = getModelFile(model.id)
            if (file.exists() && file.length() > 0) {
                model.copy(
                    path = file.absolutePath, 
                    isDownloaded = true, 
                    status = if (model.id == _activeModelId.value) LocalModelStatus.Ready else LocalModelStatus.NotLoaded
                )
            } else {
                model.copy(
                    isDownloaded = false, 
                    status = LocalModelStatus.NotLoaded,
                    downloadProgress = null
                )
            }
        }
        _models.value = updatedModels
    }

    private fun getModelFile(modelId: String): File {
        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        return File(dir, "$modelId.bin")
    }

    fun downloadModel(modelId: String) {
        val model = _models.value.find { it.id == modelId } ?: return
        if (model.isDownloaded || model.status == LocalModelStatus.Downloading) return

        val downloadId = downloader.downloadModel(model)
        
        scope.launch {
            downloader.observeDownloadProgress(downloadId)
                .onCompletion {
                    refreshModelStatus()
                }
                .collect { progress ->
                    _models.update { currentList ->
                        currentList.map { 
                            if (it.id == modelId) {
                                it.copy(
                                    status = if (progress >= 1f) LocalModelStatus.NotLoaded else LocalModelStatus.Downloading,
                                    downloadProgress = progress
                                )
                            } else it 
                        }
                    }
                }
        }
    }

    fun registerModel(descriptor: LocalModelDescriptor) {
        _models.update { currentList ->
            if (currentList.any { it.id == descriptor.id }) {
                currentList.map { if (it.id == descriptor.id) descriptor else it }
            } else {
                currentList + descriptor
            }
        }
        refreshModelStatus()
    }

    suspend fun loadModel(id: String): Result<Unit> {
        val descriptor = _models.value.find { it.id == id } 
            ?: return Result.failure(Exception("Model not found"))
        
        if (!descriptor.isDownloaded) {
            return Result.failure(Exception("Model not downloaded"))
        }

        _activeModelId.value = id
        _models.update { currentList ->
            currentList.map { 
                if (it.id == id) it.copy(status = LocalModelStatus.Loading) else it 
            }
        }

        val result = engine.load(descriptor)
        
        if (result.isSuccess) {
            _models.update { currentList ->
                currentList.map { 
                    if (it.id == id) it.copy(status = LocalModelStatus.Ready) else it 
                }
            }
        } else {
            _models.update { currentList ->
                currentList.map { 
                    if (it.id == id) it.copy(status = LocalModelStatus.Error) else it 
                }
            }
            _activeModelId.value = null
        }
        
        return result
    }

    suspend fun unloadActiveModel() {
        engine.unload()
        val currentId = _activeModelId.value
        _activeModelId.value = null
        _models.update { currentList ->
            currentList.map { 
                if (it.id == currentId) it.copy(status = LocalModelStatus.NotLoaded) else it 
            }
        }
    }

    fun deleteModel(modelId: String) {
        scope.launch {
            if (_activeModelId.value == modelId) {
                unloadActiveModel()
            }
            val file = getModelFile(modelId)
            if (file.exists()) {
                file.delete()
            }
            refreshModelStatus()
        }
    }
}
