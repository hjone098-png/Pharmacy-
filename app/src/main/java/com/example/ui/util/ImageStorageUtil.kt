package com.example.ui.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageUtil {
    private const val TAG = "ImageStorageUtil"
    private const val IMAGES_DIR = "medication_images"

    /**
     * Copies an image from a content/external Uri into app internal storage permanently.
     * Returns the absolute path of the saved internal file, or empty string on failure.
     */
    suspend fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val imagesFolder = File(context.filesDir, IMAGES_DIR)
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs()
            }

            val extension = getExtensionFromUri(context, sourceUri) ?: "jpg"
            val fileName = "med_img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"
            val destFile = File(imagesFolder, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Saved medication image internally at: ${destFile.absolutePath}")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image internally: ${e.message}", e)
            sourceUri.toString() // Fallback to uri string if copy fails
        }
    }

    /**
     * Checks if a string path points to an image in the app's internal storage directory.
     */
    fun isInternalImage(context: Context, path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val internalDir = File(context.filesDir, IMAGES_DIR).absolutePath
        return path.startsWith(internalDir)
    }

    /**
     * Deletes an image from internal storage if it exists in the medication_images directory.
     */
    fun deleteInternalImage(context: Context, path: String?) {
        if (isInternalImage(context, path)) {
            try {
                File(path!!).delete()
            } catch (e: Exception) {
                Log.e(TAG, "Could not delete image at $path: ${e.message}")
            }
        }
    }

    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        return try {
            val type = context.contentResolver.getType(uri)
            when (type) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> "jpg"
            }
        } catch (e: Exception) {
            "jpg"
        }
    }
}
