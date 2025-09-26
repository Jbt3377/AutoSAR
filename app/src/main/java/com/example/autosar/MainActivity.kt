package com.example.autosar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.autosar.compostables.Crosshair
import com.example.autosar.compostables.IPPMarker
import com.example.autosar.compostables.RangeRings
import com.example.autosar.models.LocationViewModel
import com.example.autosar.models.MarkerViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val markerViewModel: MarkerViewModel by viewModels()

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            locationViewModel.setPermissionGranted(isGranted)
            if (isGranted) {
                locationViewModel.fetchLastLocation(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationViewModel.setPermissionGranted(granted)
        if (!granted) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationViewModel.fetchLastLocation(this)
        }

        setContent {
            AppContent(locationViewModel, markerViewModel)
        }
    }
}

@Composable
fun AppContent(locationViewModel: LocationViewModel, markerViewModel: MarkerViewModel) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MapboxMapScreen(locationViewModel, markerViewModel)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapboxMapScreen(
    locationViewModel: LocationViewModel,
    markerViewModel: MarkerViewModel
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(2.0)
            center(Point.fromLngLat(-98.0, 39.5))
        }
    }

    val userLocation by locationViewModel.userLocation.collectAsState()
    val hasPermission by locationViewModel.hasPermission.collectAsState()
    val markers by markerViewModel.markers.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var pendingPoint by remember { mutableStateOf<Point?>(null) }

    var fourthRingRadius by remember { mutableStateOf<Double?>(null) }
    var radiusInput by remember { mutableStateOf("") }

    LaunchedEffect(userLocation) {
        userLocation?.let { point ->
            mapViewportState.setCameraOptions {
                zoom(14.0)
                center(point)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = Style.OUTDOORS) },
            onMapLongClickListener = { point ->
                pendingPoint = point
                showForm = true
                true
            }
        ) {
            if (markers.isNotEmpty()) {
                val centerPoint = markers[0]
                IPPMarker(centerPoint)

                val rangeRingConfigs = listOf(
                    300.0,
                    1000.0,
                    2400.0,
                    fourthRingRadius ?: 12_800.0
                )
                RangeRings(rangeRingConfigs, centerPoint)
            }
        }

        Crosshair(modifier = Modifier.align(Alignment.Center))

        if (showForm && pendingPoint != null) {
            Dialog(onDismissRequest = { showForm = false }) {
                Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Add Range Rings", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = radiusInput,
                            onValueChange = { radiusInput = it },
                            label = { Text("Outer ring radius (m)") },
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            TextButton(onClick = { showForm = false }) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val point = pendingPoint
                                    if (point != null) {
                                        markerViewModel.addMarker(point)
                                        // parse the number and update the radius state
                                        fourthRingRadius = radiusInput.toDoubleOrNull()
                                    }

                                    // reset dialog state
                                    showForm = false
                                    radiusInput = ""
                                    pendingPoint = null
                                }
                            ) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}
