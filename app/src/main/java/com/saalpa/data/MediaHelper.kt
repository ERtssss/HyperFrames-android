package com.saalpa.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MediaHelper {
    private const val TAG = "MediaHelper"

    /**
     * Converts a content Uri (photo or audio) to a Base64 data URL string
     * for direct, offline embedding in WebView HTML without network/CORS restrictions.
     */
    fun uriToDataUrl(context: Context, uri: Uri, mimeType: String): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return null
            inputStream.close()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:$mimeType;base64,$base64"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert URI to data URL: ${e.message}", e)
            null
        }
    }

    /**
     * Copies a content Uri to internal storage file and returns its absolute path.
     */
    fun copyUriToInternalFile(context: Context, uri: Uri, subDirName: String, prefix: String, extension: String): File? {
        return try {
            val dir = File(context.filesDir, subDirName).apply { mkdirs() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy URI to internal file: ${e.message}", e)
            null
        }
    }
}
