package com.msa.lagents.data.workflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.msa.lagents.R

class WorkflowNotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workflows",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status of background AI workflows"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showApprovalRequired(workflowName: String, runId: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setContentTitle("Approval Required")
            .setContentText("Workflow '$workflowName' is waiting for your approval.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(runId.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "workflow_notifications"
    }
}
