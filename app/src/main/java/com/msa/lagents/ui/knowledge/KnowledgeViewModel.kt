package com.msa.lagents.ui.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.knowledge.KnowledgeRepository
import com.msa.lagents.data.knowledge.SearchResult
import com.msa.lagents.data.local.knowledge.KnowledgeCollectionEntity
import com.msa.lagents.data.local.knowledge.KnowledgeDocumentEntity
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
import java.io.InputStream

data class KnowledgeUiState(
    val collections: List<KnowledgeCollectionEntity> = emptyList(),
    val selectedCollectionId: String? = null,
    val documents: List<KnowledgeDocumentEntity> = emptyList(),
    val isImporting: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
)

class KnowledgeViewModel(
    private val repository: KnowledgeRepository,
) : ViewModel() {

    private val _selectedCollectionId = MutableStateFlow<String?>(null)
    private val _isImporting = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<KnowledgeUiState> = combine(
        combine(
            repository.collections,
            _selectedCollectionId,
            _isImporting,
            ::Triple
        ),
        combine(
            _searchQuery,
            _searchResults,
            _selectedCollectionId.flatMapLatest { id ->
                if (id != null) repository.observeDocuments(id) else flowOf(emptyList())
            },
            ::Triple
        )
    ) { (collections, selectedId, importing), (query, results, documents) ->
        KnowledgeUiState(
            collections = collections,
            selectedCollectionId = selectedId,
            documents = documents,
            isImporting = importing,
            searchQuery = query,
            searchResults = results
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KnowledgeUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val collectionId = _selectedCollectionId.value ?: return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searchResults.value = repository.search(collectionId, query)
        }
    }

    fun selectCollection(id: String?) {
        _selectedCollectionId.value = id
    }

    fun createCollection(name: String, description: String) {
        viewModelScope.launch {
            repository.createCollection(name, description)
        }
    }

    fun deleteCollection(id: String) {
        viewModelScope.launch {
            if (_selectedCollectionId.value == id) {
                _selectedCollectionId.value = null
            }
            repository.deleteCollection(id)
        }
    }

    fun importDocument(title: String, uri: String, inputStream: InputStream) {
        val collectionId = _selectedCollectionId.value ?: return
        viewModelScope.launch {
            _isImporting.value = true
            try {
                repository.importDocument(collectionId, title, uri, inputStream)
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    class Factory(private val repository: KnowledgeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KnowledgeViewModel(repository) as T
        }
    }
}
