package com.msa.lagents.data.local.provider

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs ORDER BY displayName ASC")
    fun observeProviderConfigs(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs WHERE id = :id")
    fun observeProviderConfig(id: String): Flow<ProviderConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProviderConfig(providerConfig: ProviderConfigEntity)

    @Query("UPDATE provider_configs SET enabled = :enabled, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun setProviderEnabled(
        id: String,
        enabled: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM provider_configs WHERE id = :id")
    suspend fun deleteProviderConfig(id: String)
}
