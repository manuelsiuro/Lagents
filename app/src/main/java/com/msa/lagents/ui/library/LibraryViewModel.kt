package com.msa.lagents.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.library.LibraryOverview
import com.msa.lagents.data.library.LibraryRepository
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.tool.ToolConfigEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {
    private val _editingAgent = MutableStateFlow<AgentEntity?>(null)
    val editingAgent: StateFlow<AgentEntity?> = _editingAgent

    private val _editingPrompt = MutableStateFlow<PromptEntity?>(null)
    val editingPrompt: StateFlow<PromptEntity?> = _editingPrompt

    private val _editingSkill = MutableStateFlow<SkillEntity?>(null)
    val editingSkill: StateFlow<SkillEntity?> = _editingSkill

    private val _editingToolConfig = MutableStateFlow<ToolConfigEntity?>(null)
    val editingToolConfig: StateFlow<ToolConfigEntity?> = _editingToolConfig

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

    // Agent CRUD
    fun startEditingAgent(id: String) {
        val agent = uiState.value.agents.find { it.id == id }
        _editingAgent.value = agent
    }

    fun stopEditingAgent() {
        _editingAgent.value = null
    }

    fun updateAgent(agent: AgentEntity) {
        viewModelScope.launch {
            libraryRepository.updateAgent(agent)
            _editingAgent.value = null
        }
    }

    fun archiveAgent(id: String) {
        viewModelScope.launch {
            libraryRepository.archiveAgent(id)
        }
    }

    fun restoreAgent(id: String) {
        viewModelScope.launch {
            libraryRepository.restoreAgent(id)
        }
    }

    fun deleteAgent(id: String) {
        viewModelScope.launch {
            libraryRepository.deleteAgent(id)
        }
    }

    fun duplicateAgent(id: String) {
        viewModelScope.launch {
            val agent = uiState.value.agents.find { it.id == id }
            if (agent != null) {
                libraryRepository.duplicateAgent(agent)
            }
        }
    }

    // Prompt CRUD
    fun startEditingPrompt(id: String) {
        val prompt = uiState.value.prompts.find { it.id == id }
        _editingPrompt.value = prompt
    }

    fun stopEditingPrompt() {
        _editingPrompt.value = null
    }

    fun updatePrompt(prompt: PromptEntity) {
        viewModelScope.launch {
            libraryRepository.updatePrompt(prompt)
            _editingPrompt.value = null
        }
    }

    fun archivePrompt(id: String) {
        viewModelScope.launch {
            libraryRepository.archivePrompt(id)
        }
    }

    fun restorePrompt(id: String) {
        viewModelScope.launch {
            libraryRepository.restorePrompt(id)
        }
    }

    fun deletePrompt(id: String) {
        viewModelScope.launch {
            libraryRepository.deletePrompt(id)
        }
    }

    fun duplicatePrompt(id: String) {
        viewModelScope.launch {
            val prompt = uiState.value.prompts.find { it.id == id }
            if (prompt != null) {
                libraryRepository.duplicatePrompt(prompt)
            }
        }
    }

    // Skill CRUD
    fun startEditingSkill(id: String) {
        val skill = uiState.value.skills.find { it.id == id }
        _editingSkill.value = skill
    }

    fun stopEditingSkill() {
        _editingSkill.value = null
    }

    fun updateSkill(skill: SkillEntity) {
        viewModelScope.launch {
            libraryRepository.updateSkill(skill)
            _editingSkill.value = null
        }
    }

    fun archiveSkill(id: String) {
        viewModelScope.launch {
            libraryRepository.archiveSkill(id)
        }
    }

    fun restoreSkill(id: String) {
        viewModelScope.launch {
            libraryRepository.restoreSkill(id)
        }
    }

    fun deleteSkill(id: String) {
        viewModelScope.launch {
            libraryRepository.deleteSkill(id)
        }
    }

    fun duplicateSkill(id: String) {
        viewModelScope.launch {
            val skill = uiState.value.skills.find { it.id == id }
            if (skill != null) {
                libraryRepository.duplicateSkill(skill)
            }
        }
    }

    // Tool Config CRUD
    fun startEditingToolConfig(id: String) {
        val tool = uiState.value.tools.find { it.id == id }
        _editingToolConfig.value = tool
    }

    fun stopEditingToolConfig() {
        _editingToolConfig.value = null
    }

    fun updateToolConfig(toolConfig: ToolConfigEntity) {
        viewModelScope.launch {
            libraryRepository.updateToolConfig(toolConfig)
            _editingToolConfig.value = null
        }
    }

    fun deleteToolConfig(toolKey: String) {
        viewModelScope.launch {
            libraryRepository.deleteToolConfig(toolKey)
        }
    }

    fun setToolEnabled(toolKey: String, enabled: Boolean) {
        viewModelScope.launch {
            libraryRepository.setToolEnabled(toolKey, enabled)
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
