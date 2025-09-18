package com.example.autosar.models

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<Point?>(null)
    private val _hasPermission = MutableStateFlow(false)

    val userLocation: StateFlow<Point?> = _userLocation
    val hasPermission: StateFlow<Boolean> = _hasPermission

    fun setPermissionGranted(granted: Boolean) {
        _hasPermission.value = granted
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchLastLocation(activity: ComponentActivity) {
        if (!_hasPermission.value) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _userLocation.value = Point.fromLngLat(location.longitude, location.latitude)
            } else {
                // Handle null location (no cached fix)
                viewModelScope.launch {
                    _userLocation.emit(null)
                }
            }
        }
    }
}