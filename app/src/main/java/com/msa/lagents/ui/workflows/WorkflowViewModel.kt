package com.msa.lagents.ui.workflows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.local.workflow.WorkflowDefinitionEntity
import com.msa.lagents.data.local.workflow.WorkflowRunEntity
import com.msa.lagents.data.workflow.WorkflowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkflowUiState(
    val definitions: List<WorkflowDefinitionEntity> = emptyList(),
    val selectedWorkflowId: String? = null,
    val runs: List<WorkflowRunEntity> = emptyList(),
)

class WorkflowViewModel(
    private val repository: WorkflowRepository,
) : ViewModel() {

    private val _selectedWorkflowId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WorkflowUiState> = combine(
        repository.definitions,
        _selectedWorkflowId,
        _selectedWorkflowId.flatMapLatest { id ->
            if (id != null) repository.observeRuns(id) else flowOf(emptyList())
        }
    ) { definitions, selectedId, runs ->
        WorkflowUiState(
            definitions = definitions,
            selectedWorkflowId = selectedId,
            runs = runs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkflowUiState()
    )

    fun selectWorkflow(id: String?) {
        _selectedWorkflowId.value = id
    }

    fun createWorkflow(name: String, goal: String, agentId: String) {
        viewModelScope.launch {
            repository.createDefinition(name, goal, agentId)
        }
    }

    fun startWorkflow(id: String) {
        viewModelScope.launch {
            repository.startRun(id)
        }
    }

    fun deleteWorkflow(id: String) {
        viewModelScope.launch {
            if (_selectedWorkflowId.value == id) {
                _selectedWorkflowId.value = null
            }
            repository.deleteDefinition(id)
        }
    }

    fun provideApproval(runId: String, approved: Boolean) {
        viewModelScope.launch {
            repository.provideApproval(runId, approved)
        }
    }

    class Factory(private val repository: WorkflowRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkflowViewModel(repository) as T
        }
    }
}
