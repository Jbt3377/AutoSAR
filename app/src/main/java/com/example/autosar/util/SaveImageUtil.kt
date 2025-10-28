package com.example.autosar.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

object SaveImageUtil {

    /**
     * Saves a bitmap to the Downloads directory as a PNG.
     * Returns the Uri if successful, or null otherwise.
     */
    fun saveBitmapToDownloads(
        context: Context,
        bitmap: Bitmap?,
        fileName: String
    ): android.net.Uri? {
        val validFileName = fileName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$validFileName.png")
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        try {
            uri?.let {
                resolver.openOutputStream(it)?.use { out ->
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
            }
        } catch (e: IOException) {
            Log.e("GetSatelliteImageUtil", "Error saving bitmap: ${e.message}")
            return null
        }

        return uri
    }
}