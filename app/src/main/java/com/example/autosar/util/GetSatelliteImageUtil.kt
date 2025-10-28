package com.example.autosar.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.Size
import com.mapbox.maps.Snapshotter
import com.mapbox.maps.Style
import java.io.IOException

object GetSatelliteImageUtil {

    private var mapSnapshotter: Snapshotter? = null

    /**
     * Captures a fixed-size satellite bitmap centered at the given point.
     *
     * @param context Android context
     * @param center Center coordinate (Lng/Lat)
     * @param zoom Zoom level (default = 16.0)
     * @param width Width of snapshot in pixels (default = 512)
     * @param height Height of snapshot in pixels (default = 512)
     * @param onResult Callback returning the Bitmap or null if failed
     */
    fun captureSatelliteTile(
        context: Context,
        center: Point,
        zoom: Double = 16.0,
        width: Float = 512f,
        height: Float = 512f,
        onResult: (Bitmap?) -> Unit
    ) {
        // Clean up old instance if still active
        mapSnapshotter?.cancel()
        mapSnapshotter?.destroy()

        val snapshotOptions = MapSnapshotOptions.Builder()
            .size(Size(width, height))
            .pixelRatio(1.0f)
            .build()

        mapSnapshotter = Snapshotter(context, snapshotOptions)

        mapSnapshotter?.setCamera(
            CameraOptions.Builder()
                .center(center)
                .zoom(zoom)
                .build()
        )

        // Use Mapbox's built-in satellite streets style
        mapSnapshotter?.setStyleUri(Style.SATELLITE)

        mapSnapshotter?.start { bitmap, error ->
            if (error != null) {
                Log.e("GetSatelliteImageUtil", "Snapshot failed: ${error}")
                onResult(null)
            } else {

                val uri = saveBitmapToDownloads(context, bitmap, "satellite_snapshot")
                if (uri != null) {
                    Log.d("GetSatelliteImageUtil", "Snapshot saved: $uri")
                } else {
                    Log.e("GetSatelliteImageUtil", "Failed to save snapshot")
                }

                onResult(bitmap)
            }

            // Destroy snapshotter after completion to free memory
            mapSnapshotter?.destroy()
            mapSnapshotter = null
        }
    }

    /**
     * Saves a bitmap to the Downloads directory as a PNG.
     * Returns the Uri if successful, or null otherwise.
     */
    private fun saveBitmapToDownloads(
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
