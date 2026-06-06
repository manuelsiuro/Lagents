package com.msa.lagents.domain.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface SpeechToTextEngine {
    fun startListening(): Flow<SpeechTranscriptEvent>
    fun stopListening()
}

interface TextToSpeechEngine {
    fun speak(text: String, profile: VoiceProfileDescriptor? = null): Flow<SpeechPlaybackState>
    fun stop()
}

@Serializable
sealed class SpeechTranscriptEvent {
    data class Interim(val text: String) : SpeechTranscriptEvent()
    data class Final(val text: String) : SpeechTranscriptEvent()
    data class Error(val message: String) : SpeechTranscriptEvent()
}

@Serializable
sealed class SpeechPlaybackState {
    object Idle : SpeechPlaybackState()
    object Playing : SpeechPlaybackState()
    object Finished : SpeechPlaybackState()
    data class Error(val message: String) : SpeechPlaybackState()
}

@Serializable
data class VoiceProfileDescriptor(
    val id: String,
    val language: String,
    val speakingRate: Float = 1.0f,
    val pitch: Float = 1.0f
)
