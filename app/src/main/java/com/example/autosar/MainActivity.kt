package com.example.autosar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.autosar.composables.subjectWizard.SubjectWizard
import com.example.autosar.composables.mapFeatures.Crosshair
import com.example.autosar.composables.mapFeatures.IPPMarker
import com.example.autosar.composables.mapFeatures.RangeRings
import com.example.autosar.data.dtos.SubjectProfile
import com.example.autosar.models.LocationViewModel
import com.example.autosar.models.MarkerViewModel
import com.example.autosar.data.repositories.LPBRepository
import com.example.autosar.services.exportGeoJsonService
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
    val context = LocalContext.current
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
    var subjectProfile by remember { mutableStateOf<SubjectProfile?>(null) }

    val lpbRepository = remember { LPBRepository(context) }

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
            if (markers.isNotEmpty() && subjectProfile != null) {
                val centerPoint = markers[0]

                IPPMarker(centerPoint)

                val lpbData = lpbRepository.getRingRadii(subjectProfile!!)
                lpbData?.ringRadii?.let { RangeRings(it, centerPoint) }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if(markers.isNotEmpty()){
                FloatingActionButton(
                    onClick = { markerViewModel.clearAllMarkers() },
                ) {
                    Icon(Icons.Filled.Clear, "Clear map")
                }

                FloatingActionButton(
                    onClick = {
                        val uri = exportGeoJsonService(
                            context = context,
                            markers = markers,
                            rangeRings = subjectProfile?.let { lpbRepository.getRingRadii(it)?.ringRadii },
                            centerPoint = markers.firstOrNull(),
                            subjectProfile = subjectProfile?.toString()
                        )
                        uri?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/geo+json"
                                putExtra(Intent.EXTRA_STREAM, it)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export GeoJSON"))
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export")
                }

            }

            // Recenter GPS Button
            if (hasPermission) {
                FloatingActionButton(
                    onClick = {
                        userLocation?.let { point ->
                            mapViewportState.setCameraOptions {
                                zoom(14.0)
                                center(point)
                            }
                        } ?: run {
                            locationViewModel.fetchLastLocation(context as ComponentActivity)
                        }
                    }
                ) {
                    Icon(Icons.Filled.Place, "Recenter on my location")
                }
            }
        }

        Crosshair(modifier = Modifier.align(Alignment.Center))

        if (showForm && pendingPoint != null) {
            SubjectWizard(
                pendingPoint = pendingPoint!!,
                repository = lpbRepository,
                onDismiss = { showForm = false },
                onConfirm = { point, profile ->
                    markerViewModel.addMarker(point)
                    subjectProfile = profile
                    showForm = false
                    pendingPoint = null
                }
            )
        }
    }
}

