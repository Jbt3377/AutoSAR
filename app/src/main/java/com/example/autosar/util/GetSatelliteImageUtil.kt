package com.example.autosar.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.autosar.util.SaveImageUtil
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.Size
import com.mapbox.maps.Snapshotter
import com.mapbox.maps.Style

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

                val uri = SaveImageUtil.saveBitmapToDownloads(context, bitmap,"satellite_snapshot_${System.currentTimeMillis()}")
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
}
