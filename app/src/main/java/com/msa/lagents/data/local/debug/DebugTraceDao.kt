package com.msa.lagents.data.local.debug

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugTraceDao {
    @Query("SELECT * FROM debug_traces ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecentTraces(limit: Int): Flow<List<DebugTraceEntity>>

    @Query("SELECT * FROM debug_traces WHERE conversationId = :conversationId ORDER BY createdAtMillis DESC")
    fun observeConversationTraces(conversationId: String): Flow<List<DebugTraceEntity>>

    @Query("SELECT * FROM debug_traces WHERE workflowRunId = :workflowRunId ORDER BY createdAtMillis DESC")
    fun observeWorkflowTraces(workflowRunId: String): Flow<List<DebugTraceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrace(trace: DebugTraceEntity)

    @Query("DELETE FROM debug_traces WHERE createdAtMillis < :olderThanMillis")
    suspend fun deleteTracesOlderThan(olderThanMillis: Long)
}
