package com.msa.lagents.data.local.tool

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolConfigDao {
    @Query("SELECT * FROM tool_configs ORDER BY category ASC, displayName ASC")
    fun observeToolConfigs(): Flow<List<ToolConfigEntity>>

    @Query("SELECT * FROM tool_configs WHERE toolKey = :toolKey")
    fun observeToolConfig(toolKey: String): Flow<ToolConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertToolConfig(toolConfig: ToolConfigEntity)

    @Query("UPDATE tool_configs SET enabled = :enabled, updatedAtMillis = :updatedAtMillis WHERE toolKey = :toolKey")
    suspend fun setToolEnabled(
        toolKey: String,
        enabled: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM tool_configs WHERE toolKey = :toolKey")
    suspend fun deleteToolConfig(toolKey: String)
}
