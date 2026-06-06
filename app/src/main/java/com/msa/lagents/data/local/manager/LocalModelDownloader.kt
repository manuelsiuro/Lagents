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
            if (cursor.moveToFirst()) {
                val bytesDownloaded = cursor.getInt(cursor.getColumnIndexAt(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal = cursor.getInt(cursor.getColumnIndexAt(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                
                if (cursor.getInt(cursor.getColumnIndexAt(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading = false
                    emit(1f)
                } else {
                    if (bytesTotal > 0) {
                        emit(bytesDownloaded.toFloat() / bytesTotal)
                    }
                }
            }
            cursor.close()
            if (isDownloading) {
                delay(1000)
            }
        }
    }

    private fun Cursor.getColumnIndexAt(columnName: String): Int {
        return getColumnIndex(columnName)
    }

    fun getDownloadedFile(downloadId: Long): File? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndexAt(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val uriString = cursor.getString(cursor.getColumnIndexAt(DownloadManager.COLUMN_LOCAL_URI))
                cursor.close()
                return File(Uri.parse(uriString).path!!)
            }
        }
        cursor.close()
        return null
    }
}
