package com.msa.lagents.data.voice

import com.msa.lagents.data.local.voice.VoiceDao
import com.msa.lagents.data.local.voice.VoiceProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VoiceRepository(
    private val voiceDao: VoiceDao,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val profiles: Flow<List<VoiceProfileEntity>> = voiceDao.observeProfiles()

    fun observeProfile(id: String): Flow<VoiceProfileEntity?> {
        return voiceDao.observeProfile(id)
    }

    suspend fun upsertProfile(name: String, language: String, speakingRate: Float, pitch: Float) {
        val now = nowMillis()
        voiceDao.upsertProfile(
            VoiceProfileEntity(
                id = idGenerator(),
                name = name,
                language = language,
                speakingRate = speakingRate,
                pitch = pitch,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun deleteProfile(id: String) {
        voiceDao.deleteProfile(id)
    }
}
