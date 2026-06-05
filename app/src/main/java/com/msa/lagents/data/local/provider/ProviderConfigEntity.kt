package com.msa.lagents.data.local.provider

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "provider_configs",
    indices = [
        Index(value = ["providerType"]),
        Index(value = ["enabled"]),
    ],
)
data class ProviderConfigEntity(
    @PrimaryKey val id: String,
    val providerType: String,
    val displayName: String,
    val enabled: Boolean,
    val apiKeyAlias: String?,
    val baseUrl: String?,
    val defaultModelId: String?,
    val modelOverridesJson: String,
    val budgetPolicyJson: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
