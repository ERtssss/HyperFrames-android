package com.saalpa.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.saalpa.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ImportedZipAsset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relativePath: String,
    val folderName: String,
    val file: File,
    val mimeType: String,
    val mediaType: MediaType,
    val sizeBytes: Long,
    val dataUrl: String? = null
) {
    val formattedSize: String
        get() {
            return when {
                sizeBytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes / (1024f * 1024f))
                sizeBytes >= 1024 -> "${sizeBytes / 1024} KB"
                else -> "$sizeBytes B"
            }
        }
}

class ZipMediaManager(private val context: Context) {
    private val TAG = "ZipMediaManager"
    private val baseDir: File = File(context.filesDir, "imported_media_hub").apply { mkdirs() }

    suspend fun unpackZip(uri: Uri): List<ImportedZipAsset> = withContext(Dispatchers.IO) {
        val importedList = mutableListOf<ImportedZipAsset>()
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return@withContext emptyList()
            val zipInputStream = ZipInputStream(inputStream)
            var entry: ZipEntry? = zipInputStream.nextEntry

            val sessionFolder = File(baseDir, "pkg_${System.currentTimeMillis()}").apply { mkdirs() }

            while (entry != null) {
                val entryName = entry.name
                // Skip directories, mac os metadata, hidden files
                if (!entry.isDirectory &&
                    !entryName.startsWith("__MACOSX") &&
                    !entryName.contains("/.") &&
                    !entryName.startsWith(".")
                ) {
                    val extension = entryName.substringAfterLast('.', "").lowercase()
                    val mediaType = getMediaTypeForExtension(extension)

                    if (mediaType != null) {
                        val sanitizedName = File(entryName).name
                        val relativePath = entryName.replace('\\', '/')
                        val folderName = if (relativePath.contains('/')) {
                            relativePath.substringBeforeLast('/').substringAfterLast('/')
                        } else {
                            "Корень (Root)"
                        }

                        val targetFile = File(sessionFolder, entryName)
                        targetFile.parentFile?.mkdirs()

                        FileOutputStream(targetFile).use { output ->
                            zipInputStream.copyTo(output)
                        }

                        val mimeType = getMimeTypeForExtension(extension)
                        val dataUrl = createDataUrl(targetFile, mimeType)

                        importedList.add(
                            ImportedZipAsset(
                                name = sanitizedName,
                                relativePath = relativePath,
                                folderName = folderName,
                                file = targetFile,
                                mimeType = mimeType,
                                mediaType = mediaType,
                                sizeBytes = targetFile.length(),
                                dataUrl = dataUrl
                            )
                        )
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error unpacking ZIP: ${e.message}", e)
        }
        importedList
    }

    suspend fun importFiles(uris: List<Uri>): List<ImportedZipAsset> = withContext(Dispatchers.IO) {
        val importedList = mutableListOf<ImportedZipAsset>()
        val customFolder = File(baseDir, "direct_imports").apply { mkdirs() }

        for (uri in uris) {
            try {
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val guessedExt = when {
                    mime.contains("audio") || mime.contains("mp3") -> "mp3"
                    mime.contains("video") || mime.contains("mp4") -> "mp4"
                    mime.contains("png") -> "png"
                    mime.contains("jpeg") || mime.contains("jpg") -> "jpg"
                    else -> "dat"
                }
                val fileName = "media_${System.currentTimeMillis()}_${(100..999).random()}.$guessedExt"
                val targetFile = File(customFolder, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val mediaType = getMediaTypeForExtension(guessedExt) ?: MediaType.PHOTO
                val dataUrl = createDataUrl(targetFile, mime)

                importedList.add(
                    ImportedZipAsset(
                        name = fileName,
                        relativePath = "direct_imports/$fileName",
                        folderName = "Загрузки",
                        file = targetFile,
                        mimeType = mime,
                        mediaType = mediaType,
                        sizeBytes = targetFile.length(),
                        dataUrl = dataUrl
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error importing file: ${e.message}", e)
            }
        }
        importedList
    }

    suspend fun loadAllImportedAssets(): List<ImportedZipAsset> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ImportedZipAsset>()
        if (!baseDir.exists()) return@withContext list

        baseDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val ext = file.extension.lowercase()
                val mediaType = getMediaTypeForExtension(ext)
                if (mediaType != null) {
                    val relativePath = file.relativeTo(baseDir).path.replace('\\', '/')
                    val folderName = if (relativePath.contains('/')) {
                        relativePath.substringBeforeLast('/').substringAfterLast('/')
                    } else {
                        "Корень (Root)"
                    }
                    val mimeType = getMimeTypeForExtension(ext)
                    val dataUrl = createDataUrl(file, mimeType)

                    list.add(
                        ImportedZipAsset(
                            name = file.name,
                            relativePath = relativePath,
                            folderName = folderName,
                            file = file,
                            mimeType = mimeType,
                            mediaType = mediaType,
                            sizeBytes = file.length(),
                            dataUrl = dataUrl
                        )
                    )
                }
            }
        }
        list
    }

    suspend fun deleteAsset(asset: ImportedZipAsset) = withContext(Dispatchers.IO) {
        try {
            if (asset.file.exists()) {
                asset.file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file ${asset.file.path}: ${e.message}", e)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            baseDir.deleteRecursively()
            baseDir.mkdirs()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all media: ${e.message}", e)
        }
    }

    private fun getMediaTypeForExtension(ext: String): MediaType? {
        return when (ext) {
            "mp3", "wav", "ogg", "m4a", "aac", "flac" -> MediaType.BGM
            "mp4", "webm", "mov", "mkv", "3gp" -> MediaType.VIDEO
            "png", "jpg", "jpeg", "webp", "svg", "gif" -> MediaType.PHOTO
            else -> null
        }
    }

    private fun getMimeTypeForExtension(ext: String): String {
        return when (ext) {
            "mp3" -> "audio/mp3"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a", "aac" -> "audio/mp4"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }

    private fun createDataUrl(file: File, mimeType: String): String? {
        return try {
            if (file.length() <= 8 * 1024 * 1024) { // Only base64 encode if <= 8MB
                val bytes = file.readBytes()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:$mimeType;base64,$base64"
            } else {
                file.toURI().toString()
            }
        } catch (e: Exception) {
            file.toURI().toString()
        }
    }
}
