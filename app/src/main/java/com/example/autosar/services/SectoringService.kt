package com.example.autosar.services

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SectoringService {

    private val _sectors = MutableStateFlow<List<Polygon>>(emptyList())
    val sectors = _sectors.asStateFlow()

    fun sectorHub(centerPoint: Point) {
        val size = 0.01
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

    fun clearSectors() {
        _sectors.value = emptyList()
    }
}