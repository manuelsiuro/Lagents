package com.msa.lagents.data.local.manager

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.msa.lagents.domain.local.LocalModelDescriptor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class LocalModelDownloader(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadModel(model: LocalModelDescriptor): Long {
        val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
            .setTitle("Downloading ${model.displayName}")
            .setDescription("Gemma Local Model")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "${model.id}.bin")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
    }

    fun observeDownloadProgress(downloadId: Long): Flow<Float> = flow {
        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
                
                val downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesDownloaded = if (downloadedIdx != -1) cursor.getLong(downloadedIdx) else 0L
                
                val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val bytesTotal = if (totalIdx != -1) cursor.getLong(totalIdx) else 0L

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isDownloading = false
                        emit(1f)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        isDownloading = false
                        // We could throw but let's just finish the flow and let manager refresh
                    }
                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                        if (bytesTotal > 0) {
                            emit(bytesDownloaded.toFloat() / bytesTotal)
                        } else {
                            emit(0f)
                        }
                    }
                }
            } else {
                isDownloading = false
            }
            cursor?.close()
            if (isDownloading) {
                delay(1000)
            }
        }
    }

    fun getDownloadedFile(downloadId: Long): File? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val uriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val uriString = if (uriIdx != -1) cursor.getString(uriIdx) else null
                cursor.close()
                return uriString?.let { File(Uri.parse(it).path!!) }
            }
        }
        cursor?.close()
        return null
    }
}
