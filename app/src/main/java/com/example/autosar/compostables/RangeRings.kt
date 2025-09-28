package com.example.autosar.compostables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation

@Composable
fun RangeRings(rangeRingConfigs: List<Double>, centerPoint: Point) {
    // Define the percentage labels
    val percentageLabels = listOf("25%", "50%", "75%", "95%")

    rangeRingConfigs.forEachIndexed { index, radiusInMeters ->

        // Skip ring placement if no data available
        if (radiusInMeters == 0.0) {
            return@forEachIndexed
        }

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

        val labelPrefix = percentageLabels.getOrNull(index) ?: ""

        // Range Ring Annotation
        MapFeatureAnnotation(centerPoint, -radiusInMeters, "${labelPrefix}: ${radiusInMeters}m")
    }
}