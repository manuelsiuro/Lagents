package com.msa.lagents.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.local.debug.DebugTraceDao
import com.msa.lagents.data.local.debug.DebugTraceEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DebugUiState(
    val recentTraces: List<DebugTraceEntity> = emptyList(),
)

class DebugViewModel(
    private val debugTraceDao: DebugTraceDao,
) : ViewModel() {

    val uiState: StateFlow<DebugUiState> = debugTraceDao.observeRecentTraces(20)
        .map { DebugUiState(recentTraces = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DebugUiState()
        )

    class Factory(private val debugTraceDao: DebugTraceDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DebugViewModel(debugTraceDao) as T
        }
    }
}
