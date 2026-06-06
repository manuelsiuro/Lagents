package com.msa.lagents.data.local.workflow

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflow_definitions ORDER BY updatedAtMillis DESC")
    fun observeDefinitions(): Flow<List<WorkflowDefinitionEntity>>

    @Query("SELECT * FROM workflow_definitions WHERE id = :id")
    fun observeDefinition(id: String): Flow<WorkflowDefinitionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDefinition(definition: WorkflowDefinitionEntity)

    @Query("DELETE FROM workflow_definitions WHERE id = :id")
    suspend fun deleteDefinition(id: String)

    @Query("SELECT * FROM workflow_runs WHERE workflowId = :workflowId ORDER BY startedAtMillis DESC")
    fun observeRunsForWorkflow(workflowId: String): Flow<List<WorkflowRunEntity>>

    @Query("SELECT * FROM workflow_runs WHERE id = :id")
    fun observeRun(id: String): Flow<WorkflowRunEntity?>

    @Query("SELECT * FROM workflow_runs WHERE id = :id")
    suspend fun getRun(id: String): WorkflowRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRun(run: WorkflowRunEntity)

    @Query("DELETE FROM workflow_runs WHERE workflowId = :workflowId")
    suspend fun deleteRunsForWorkflow(workflowId: String)
}
