package com.msa.lagents.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.msa.lagents.domain.voice.SpeechPlaybackState
import com.msa.lagents.domain.voice.TextToSpeechEngine
import com.msa.lagents.domain.voice.VoiceProfileDescriptor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

class AndroidTextToSpeechEngine(private val context: Context) : TextToSpeechEngine {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    override fun speak(text: String, profile: VoiceProfileDescriptor?): Flow<SpeechPlaybackState> = callbackFlow {
        val onInitListener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val ttsInstance = tts ?: return@OnInitListener
                
                profile?.let {
                    ttsInstance.setLanguage(Locale.forLanguageTag(it.language))
                    ttsInstance.setSpeechRate(it.speakingRate)
                    ttsInstance.setPitch(it.pitch)
                }

                ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        trySend(SpeechPlaybackState.Playing).getOrThrow()
                    }

                    override fun onDone(utteranceId: String?) {
                        trySend(SpeechPlaybackState.Finished).getOrThrow()
                        close()
                    }

                    override fun onError(utteranceId: String?) {
                        trySend(SpeechPlaybackState.Error("TTS Playback failed")).getOrThrow()
                        close()
                    }
                })

                val params = null // Bundle for more options if needed
                ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utterance_id")
            } else {
                trySend(SpeechPlaybackState.Error("TTS Initialization failed")).getOrThrow()
                close()
            }
        }

        tts = TextToSpeech(context, onInitListener)

        awaitClose {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        }
    }

    override fun stop() {
        tts?.stop()
    }
}
