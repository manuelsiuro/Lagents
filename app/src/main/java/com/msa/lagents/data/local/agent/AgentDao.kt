package com.msa.lagents.data.local.agent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents WHERE archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeActiveAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id")
    fun observeAgent(id: String): Flow<AgentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAgent(agent: AgentEntity)

    @Query("UPDATE agents SET archivedAtMillis = :archivedAtMillis, updatedAtMillis = :archivedAtMillis WHERE id = :id")
    suspend fun archiveAgent(id: String, archivedAtMillis: Long)

    @Query("UPDATE agents SET archivedAtMillis = NULL, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun restoreAgent(id: String, updatedAtMillis: Long)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteAgent(id: String)
}
