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

    /**
     * Deprecating example method of adding 1 polygon to map
     *
     * @param centerPoint IPP Marker
     */
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

    /**
     * Method sectors hub around the provided point
     *
     * @param centerPoint IPP Marker
     */
    fun sectorHubWithImagery(centerPoint: Point) {

        // Get satellite image of IPP Point
        GetSatelliteImageUtil.captureSatelliteTile(
            context = context,
            center = centerPoint,
            zoom = 16.5
        ) { bitmap ->
            if (bitmap != null) {

                // Extract polygons from satellite image
                val polygons = ImageSegmentationUtil.extractPolygonsFromImage(
                    context,
                    bitmap = bitmap,
                    center = centerPoint,
                    zoom = 16.5
                )

                Log.d("Polygons", "Polygon information: $polygons")

                // Draw polygons
                for(polygon in polygons){
                    val polygonConverted = Polygon.fromLngLats(listOf(polygon))
                    _sectors.value = _sectors.value + polygonConverted
                }

                val size = 0.001
                val test = listOf(
                    listOf(
                        Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() + size),
                        Point.fromLngLat(centerPoint.longitude() + size, centerPoint.latitude() + size),
                        Point.fromLngLat(centerPoint.longitude() + size, centerPoint.latitude() - size),
                        Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() - size),
                        Point.fromLngLat(centerPoint.longitude() - size, centerPoint.latitude() + size)
                    )
                )

                Log.d("Polygons Test", "Polygon information: $test")

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