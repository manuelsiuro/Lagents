package com.msa.lagents.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.library.LibraryOverview
import com.msa.lagents.data.library.LibraryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {
    val uiState: StateFlow<LibraryOverview> = libraryRepository.overview.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = LibraryOverview(),
    )

    fun createStarterAgent() {
        viewModelScope.launch {
            libraryRepository.createStarterAgent()
        }
    }

    fun createStarterPrompt() {
        viewModelScope.launch {
            libraryRepository.createStarterPrompt()
        }
    }

    fun createStarterSkill() {
        viewModelScope.launch {
            libraryRepository.createStarterSkill()
        }
    }

    fun createStarterToolConfig() {
        viewModelScope.launch {
            libraryRepository.createStarterToolConfig()
        }
    }

    class Factory(
        private val libraryRepository: LibraryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                return LibraryViewModel(libraryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
