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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.autosar.models.LocationViewModel
import com.example.autosar.models.MarkerViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation

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

    // move camera when location updates
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
                markerViewModel.addMarker(point)
                true
            }
        ) {
            Log.d("MapboxMapScreen", "Markers: $markers")

            val marker = rememberIconImage(key = R.drawable.ic_marker, painter = painterResource(R.drawable.ic_marker))

            if(markers.isNotEmpty()) {
                val centerPoint = markers[0]
                PointAnnotation(
                    point = centerPoint
                ) {
                    iconImage = marker
                    iconSize = 0.05
                }

                val circlePolygon = TurfTransformation.circle(
                    centerPoint,
                    500.0, // Radius in meters
                    360, // Steps (higher value means smoother circle)
                    TurfConstants.UNIT_METERS
                )

                PolygonAnnotation(
                    points = listOf(circlePolygon.coordinates()[0]),
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.crosshair),
            contentDescription = "Map crosshair",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp)
        )

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
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Place, "Recenter on my location")
            }
        }
    }
}
