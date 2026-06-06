package com.msa.lagents.data.workflow

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.msa.lagents.LagentsApplication
import com.msa.lagents.domain.model.GenerationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile

class WorkflowWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationHelper = WorkflowNotificationHelper(context)

    override suspend fun doWork(): Result {
        val runId = inputData.getString("runId") ?: return Result.failure()
        
        val appContainer = (applicationContext as LagentsApplication).appContainer
        val workflowDao = appContainer.database.workflowDao()
        val agentRuntime = appContainer.agentRuntime
        
        val runRecord = workflowDao.getRun(runId) ?: return Result.failure()
        val definition = workflowDao.observeDefinition(runRecord.workflowId).first() ?: return Result.failure()

        workflowDao.upsertRun(runRecord.copy(status = "Running"))

        try {
            // Background workflow loop
            agentRuntime.run(
                agentId = definition.agentId,
                conversationId = "workflow-$runId", // Isolated conversation for workflow
                userInput = definition.goal
            ).collect { event ->
                val currentRun = workflowDao.getRun(runId)!!
                when (event) {
                    is GenerationEvent.ToolCallDelta -> {
                        event.name?.let { name ->
                            workflowDao.upsertRun(currentRun.copy(
                                logs = currentRun.logs + "Agent triggered tool: $name\n"
                            ))
                        }
                    }
                    is GenerationEvent.ToolApprovalRequest -> {
                        // 1. Update DB to notify UI and store tool details
                        workflowDao.upsertRun(
                            currentRun.copy(
                                status = "Awaiting Approval",
                                pendingApprovalCallId = event.callId,
                                pendingApprovalToolName = event.toolName,
                                pendingApprovalArguments = event.argumentsJson,
                                approvalDecision = null,
                                logs = currentRun.logs + "Paused for approval: ${event.toolName}\n"
                            )
                        )

                        // 2. Show notification
                        notificationHelper.showApprovalRequired(definition.name, runId)

                        // 3. Wait for user decision in DB
                        val decision = workflowDao.observeRun(runId)
                            .map { it?.approvalDecision }
                            .filter { it != null }
                            .first()!!

                        // 4. Resume runtime
                        agentRuntime.provideApproval(decision)

                        // 5. Reset decision in DB for next potential call
                        workflowDao.upsertRun(
                            workflowDao.getRun(runId)!!.copy(
                                status = "Running",
                                pendingApprovalCallId = null,
                                pendingApprovalToolName = null,
                                pendingApprovalArguments = null,
                                approvalDecision = null
                            )
                        )
                    }
                    is GenerationEvent.Finished -> {
                        workflowDao.upsertRun(
                            workflowDao.getRun(runId)!!.copy(
                                status = "Completed",
                                finishedAtMillis = System.currentTimeMillis()
                            )
                        )
                    }
                    is GenerationEvent.Error -> {
                        workflowDao.upsertRun(
                            workflowDao.getRun(runId)!!.copy(
                                status = "Failed",
                                error = event.error.message,
                                finishedAtMillis = System.currentTimeMillis()
                            )
                        )
                    }
                    else -> {}
                }
            }
            return Result.success()
        } catch (e: Exception) {
            workflowDao.upsertRun(
                workflowDao.getRun(runId)!!.copy(
                    status = "Failed",
                    error = e.message,
                    finishedAtMillis = System.currentTimeMillis()
                )
            )
            return Result.failure()
        }
    }
}
