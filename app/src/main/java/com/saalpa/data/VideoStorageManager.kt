package com.saalpa.data

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.saalpa.model.SavedVideo
import java.io.File
import java.util.Locale

class VideoStorageManager(private val context: Context) {
    private val TAG = "VideoStorageManager"

    fun getVideosDirectory(): File {
        val dir = File(context.filesDir, "rendered_videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getAllSavedVideos(): List<SavedVideo> {
        val dir = getVideosDirectory()
        val files = dir.listFiles { file -> file.extension.lowercase(Locale.ROOT) == "mp4" } ?: emptyArray()

        return files.mapNotNull { file ->
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val durationMs = durationStr?.toLongOrNull() ?: 3000L
                val width = widthStr?.toIntOrNull() ?: 720
                val height = heightStr?.toIntOrNull() ?: 1280
                retriever.release()

                SavedVideo(
                    id = file.name,
                    title = file.nameWithoutExtension.replace("HyperFrame_", "").replace("_", " "),
                    file = file,
                    createdAt = file.lastModified(),
                    durationSec = durationMs / 1000f,
                    width = width,
                    height = height,
                    fps = 30,
                    fileSizeFormatted = formatBytes(file.length())
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error reading video file ${file.name}", e)
                null
            }
        }.sortedByDescending { it.createdAt }
    }

    fun shareVideo(video: SavedVideo) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                video.file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share HyperFrame Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing video", e)
        }
    }

    fun deleteVideo(video: SavedVideo): Boolean {
        return try {
            video.file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video", e)
            false
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }
}
