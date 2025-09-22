package com.example.autosar.compostables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation


@Composable
fun RangeRings(rangeRingConfigs: List<Double>, centerPoint: Point) {
    rangeRingConfigs.forEach { radiusInMeters ->
        val ringPolygon = TurfTransformation.circle(
            centerPoint,
            radiusInMeters,
            360,
            TurfConstants.UNIT_METERS
        )

        // Range Ring
        PolylineAnnotation(
            points = ringPolygon.coordinates()[0]
        ) {
            lineColor = Color.Black
            lineOpacity = 1.0
            lineWidth = 2.0
        }

        // Range Ring Annotation
        MapFeatureAnnotation(centerPoint, -radiusInMeters, radiusInMeters.toString() + "m")
    }
}