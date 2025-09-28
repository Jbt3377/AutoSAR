package com.example.autosar.composables.mapFeatures

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.autosar.R
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor

@Composable
fun IPPMarker(centerPoint: Point) {

    val markerIcon = rememberIconImage(key = R.drawable.ic_marker, painter = painterResource(R.drawable.ic_marker))

    PointAnnotation(
        point = centerPoint
    ) {
        iconImage = markerIcon
        iconSize = 0.04
        iconAnchor = IconAnchor.BOTTOM
    }

    MapFeatureAnnotation(centerPoint, -5.0, "IPP")
}