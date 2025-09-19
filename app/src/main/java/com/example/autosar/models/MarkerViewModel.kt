package com.example.autosar.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarkerViewModel : ViewModel() {
    private val _markers = MutableStateFlow<List<Point>>(emptyList())
    val markers: StateFlow<List<Point>> = _markers

    fun addMarker(point: Point) {
        clearAllMarkers()
        Log.d("MarkerViewModel", "Adding marker: $point")
        _markers.value += point
    }

    private fun clearAllMarkers() {
        Log.d("MarkerViewModel", "Clearing all markers")
        _markers.value = emptyList()
    }
}
