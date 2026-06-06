package com.msa.lagents.data.workflow

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.msa.lagents.data.local.workflow.WorkflowDao
import com.msa.lagents.data.local.workflow.WorkflowDefinitionEntity
import com.msa.lagents.data.local.workflow.WorkflowRunEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WorkflowRepository(
    private val context: Context,
    private val workflowDao: WorkflowDao,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val definitions: Flow<List<WorkflowDefinitionEntity>> = workflowDao.observeDefinitions()

    fun observeRuns(workflowId: String): Flow<List<WorkflowRunEntity>> {
        return workflowDao.observeRunsForWorkflow(workflowId)
    }

    fun observeRun(runId: String): Flow<WorkflowRunEntity?> {
        return workflowDao.observeRun(runId)
    }

    suspend fun createDefinition(name: String, goal: String, agentId: String) {
        val now = nowMillis()
        workflowDao.upsertDefinition(
            WorkflowDefinitionEntity(
                id = idGenerator(),
                name = name,
                goal = goal,
                agentId = agentId,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun startRun(workflowId: String) {
        val runId = idGenerator()
        val run = WorkflowRunEntity(
            id = runId,
            workflowId = workflowId,
            status = "Pending",
            startedAtMillis = nowMillis()
        )
        workflowDao.upsertRun(run)

        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<WorkflowWorker>()
            .setInputData(Data.Builder()
                .putString("runId", runId)
                .build())
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build()
        
        workManager.enqueue(request)
    }

    suspend fun deleteDefinition(id: String) {
        workflowDao.deleteRunsForWorkflow(id)
        workflowDao.deleteDefinition(id)
    }

    suspend fun provideApproval(runId: String, approved: Boolean) {
        val run = workflowDao.getRun(runId) ?: return
        workflowDao.upsertRun(run.copy(approvalDecision = approved))
    }
}
