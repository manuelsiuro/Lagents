package com.msa.lagents.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.msa.lagents.data.local.agent.AgentDao
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.conversation.ConversationDao
import com.msa.lagents.data.local.conversation.ConversationEntity
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.runtime.AgentRuntime
import com.msa.lagents.domain.voice.SpeechPlaybackState
import com.msa.lagents.domain.voice.SpeechToTextEngine
import com.msa.lagents.domain.voice.SpeechTranscriptEvent
import com.msa.lagents.domain.voice.TextToSpeechEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val currentConversationId: String? = null,
    val conversations: List<ConversationEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val agents: List<AgentEntity> = emptyList(),
    val selectedAgentId: String? = null,
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val pendingApproval: ToolApprovalState? = null,
    val citations: List<GenerationEvent.Citation> = emptyList(),
    val memorySuggestions: List<GenerationEvent.MemorySuggestion> = emptyList(),
    val workflowProgress: GenerationEvent.WorkflowProgress? = null,
    val voiceTranscript: String? = null,
    val isRecording: Boolean = false,
    val playbackState: SpeechPlaybackState = SpeechPlaybackState.Idle,
)

data class ToolApprovalState(
    val callId: String,
    val toolName: String,
    val arguments: String,
)

class ChatViewModel(
    private val agentRuntime: AgentRuntime,
    private val conversationDao: ConversationDao,
    private val agentDao: AgentDao,
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
) : ViewModel() {

    private val _currentConversationId = MutableStateFlow<String?>(null)
    private val _selectedAgentId = MutableStateFlow<String?>(null)
    private val _isGenerating = MutableStateFlow(false)
    private val _streamingText = MutableStateFlow("")
    private val _pendingApproval = MutableStateFlow<ToolApprovalState?>(null)
    private val _citations = MutableStateFlow<List<GenerationEvent.Citation>>(emptyList())
    private val _memorySuggestions = MutableStateFlow<List<GenerationEvent.MemorySuggestion>>(emptyList())
    private val _workflowProgress = MutableStateFlow<GenerationEvent.WorkflowProgress?>(null)
    private val _voiceTranscript = MutableStateFlow<String?>(null)
    private val _isRecording = MutableStateFlow(false)
    private val _playbackState = MutableStateFlow<SpeechPlaybackState>(SpeechPlaybackState.Idle)

    private var voiceJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChatUiState> = combine(
        combine(
            _currentConversationId,
            _selectedAgentId,
            _isGenerating,
            _streamingText,
            _pendingApproval
        ) { currentId, agentId, generating, streamText, approval ->
            Tuple5(currentId, agentId, generating, streamText, approval)
        },
        combine(
            _citations,
            _memorySuggestions,
            _workflowProgress
        ) { citations, suggestions, progress ->
            Tuple3(citations, suggestions, progress)
        },
        combine(
            _voiceTranscript,
            _isRecording,
            _playbackState
        ) { transcript, recording, playback ->
            Tuple3(transcript, recording, playback)
        },
        conversationDao.observeConversations(),
        agentDao.observeActiveAgents(),
        _currentConversationId.flatMapLatest { id ->
            if (id != null) conversationDao.observeMessages(id) else flowOf(emptyList())
        }
    ) { args: Array<Any> ->
        val params1 = args[0] as Tuple5<String?, String?, Boolean, String, ToolApprovalState?>
        val params2 = args[1] as Tuple3<List<GenerationEvent.Citation>, List<GenerationEvent.MemorySuggestion>, GenerationEvent.WorkflowProgress?>
        val params3 = args[2] as Tuple3<String?, Boolean, SpeechPlaybackState>
        val convs = args[3] as List<ConversationEntity>
        val agents = args[4] as List<AgentEntity>
        val msgs = args[5] as List<MessageEntity>

        ChatUiState(
            currentConversationId = params1.a,
            selectedAgentId = params1.b ?: agents.firstOrNull()?.id,
            isGenerating = params1.c,
            streamingText = params1.d,
            conversations = convs,
            agents = agents,
            messages = msgs,
            pendingApproval = params1.e,
            citations = params2.a,
            memorySuggestions = params2.b,
            workflowProgress = params2.c,
            voiceTranscript = params3.a,
            isRecording = params3.b,
            playbackState = params3.c
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    fun sendMessage(text: String) {
        val agentId = _selectedAgentId.value ?: return
        val conversationId = _currentConversationId.value ?: run {
            val newId = UUID.randomUUID().toString()
            _currentConversationId.value = newId
            viewModelScope.launch {
                val now = System.currentTimeMillis()
                conversationDao.upsertConversation(
                    ConversationEntity(id = newId, title = "New Chat", createdAtMillis = now, updatedAtMillis = now)
                )
            }
            newId
        }

        viewModelScope.launch {
            conversationDao.upsertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = "user",
                    content = text,
                    createdAtMillis = System.currentTimeMillis()
                )
            )

            _isGenerating.value = true
            _streamingText.value = ""
            _citations.value = emptyList()
            _memorySuggestions.value = emptyList()

            agentRuntime.run(agentId, conversationId, text).collect { event ->
                when (event) {
                    is GenerationEvent.TextDelta -> {
                        _streamingText.value += event.delta
                    }
                    is GenerationEvent.ToolApprovalRequest -> {
                        _pendingApproval.value = ToolApprovalState(
                            callId = event.callId,
                            toolName = event.toolName,
                            arguments = event.argumentsJson
                        )
                    }
                    is GenerationEvent.Citation -> {
                        _citations.value += event
                    }
                    is GenerationEvent.MemorySuggestion -> {
                        _memorySuggestions.value += event
                    }
                    is GenerationEvent.WorkflowProgress -> {
                        _workflowProgress.value = event
                    }
                    is GenerationEvent.Finished -> {
                        _isGenerating.value = false
                        _streamingText.value = ""
                    }
                    is GenerationEvent.Error -> {
                        _isGenerating.value = false
                        // TODO: Show error
                    }
                    else -> {}
                }
            }
        }
    }

    fun startVoiceInput() {
        voiceJob?.cancel()
        _isRecording.value = true
        _voiceTranscript.value = ""
        voiceJob = viewModelScope.launch {
            sttEngine.startListening().collect { event ->
                when (event) {
                    is SpeechTranscriptEvent.Interim -> _voiceTranscript.value = event.text
                    is SpeechTranscriptEvent.Final -> {
                        _voiceTranscript.value = event.text
                        _isRecording.value = false
                        sendMessage(event.text)
                    }
                    is SpeechTranscriptEvent.Error -> {
                        _isRecording.value = false
                        _voiceTranscript.value = null
                    }
                }
            }
        }
    }

    fun stopVoiceInput() {
        sttEngine.stopListening()
        _isRecording.value = false
        voiceJob?.cancel()
    }

    fun togglePlayback(text: String) {
        if (_playbackState.value == SpeechPlaybackState.Playing) {
            ttsEngine.stop()
        } else {
            viewModelScope.launch {
                ttsEngine.speak(text).collect { state ->
                    _playbackState.value = state
                }
            }
        }
    }

    fun approveTool(approved: Boolean) {
        viewModelScope.launch {
            agentRuntime.provideApproval(approved)
            _pendingApproval.value = null
        }
    }

    fun acceptMemorySuggestion(suggestionId: String, approved: Boolean) {
        viewModelScope.launch {
            // TODO: Persist memory if approved
            _memorySuggestions.value = _memorySuggestions.value.filter { it.id != suggestionId }
        }
    }

    fun dismissWorkflowProgress() {
        _workflowProgress.value = null
    }

    fun createNewConversation(title: String = "New Chat") {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            conversationDao.upsertConversation(
                ConversationEntity(id = id, title = title, createdAtMillis = now, updatedAtMillis = now)
            )
            _currentConversationId.value = id
        }
    }

    fun selectConversation(id: String?) {
        _currentConversationId.value = id
    }

    fun selectAgent(id: String) {
        _selectedAgentId.value = id
    }

    private data class Tuple3<A, B, C>(val a: A, val b: B, val c: C)
    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    class Factory(
        private val agentRuntime: AgentRuntime,
        private val conversationDao: ConversationDao,
        private val agentDao: AgentDao,
        private val sttEngine: SpeechToTextEngine,
        private val ttsEngine: TextToSpeechEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(agentRuntime, conversationDao, agentDao, sttEngine, ttsEngine) as T
        }
    }
}
