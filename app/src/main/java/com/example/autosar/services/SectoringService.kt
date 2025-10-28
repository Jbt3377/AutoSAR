package com.example.autosar.services

import android.content.Context
import android.util.Log
import com.example.autosar.util.GetSatelliteImageUtil
import com.example.autosar.util.ImageSegmentationUtil
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SectoringService(private val context: Context) {

    private val _sectors = MutableStateFlow<List<Polygon>>(emptyList())
    val sectors = _sectors.asStateFlow()

    fun sectorHub(centerPoint: Point) {
        val size = 0.001
        val points = listOf(
            listOf(
                Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() + size),
                Point.fromLngLat(centerPoint.longitude() + size, centerPoint.latitude() + size),
                Point.fromLngLat(centerPoint.longitude() + size, centerPoint.latitude() - size),
                Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() - size),
                Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() + size)
            )
        )
        val polygon = Polygon.fromLngLats(points)
        _sectors.value = _sectors.value + polygon
    }

    fun sectorHubWithImagery(centerPoint: Point) {
        GetSatelliteImageUtil.captureSatelliteTile(
            context = context, // Now using the context from the constructor
            center = centerPoint,
            zoom = 16.5
        ) { bitmap ->
            if (bitmap != null) {

                val polygons = ImageSegmentationUtil.extractPolygonsFromImage(bitmap)

                Log.d("Segmentation", "Extracted and added ${polygons.size} polygons")
            } else {
                Log.e("Snapshot", "Failed to get satellite image")
            }
        }
    }

    fun clearSectors() {
        _sectors.value = emptyList()
    }
}