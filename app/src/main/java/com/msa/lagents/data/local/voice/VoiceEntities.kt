package com.msa.lagents.data.local.voice

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val language: String,
    val speakingRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
