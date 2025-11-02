package com.example.autosar.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.IOException
import java.io.OutputStream

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

    fun saveMatAsImage(context: Context, mat: Mat, filename: String) {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        val imageOutStream: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AutoSAR_Debug")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageOutStream = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            // Fallback for older versions (requires WRITE_EXTERNAL_STORAGE permission)
            val imagesDir = context.getExternalFilesDir("Pictures/AutoSAR_Debug")
            val image = java.io.File(imagesDir, "$filename.jpg")
            imageOutStream = java.io.FileOutputStream(image)
        }

        imageOutStream?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}