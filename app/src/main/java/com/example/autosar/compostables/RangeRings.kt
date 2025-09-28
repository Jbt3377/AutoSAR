package com.example.autosar.compostables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation
import java.text.NumberFormat
import java.util.Locale

fun formatRangeRingLabel(index: Int, radiusInMeters: Double): String {
    val percentageLabels = listOf("25%", "50%", "75%", "95%")
    val prefix = percentageLabels.getOrNull(index).orEmpty()

    // Format with thousands separator and no decimals.
    val nf = NumberFormat.getNumberInstance(Locale.getDefault())
    nf.maximumFractionDigits = 0
    val radiusText = nf.format(radiusInMeters)

    return if (prefix.isNotEmpty())
        "$prefix: ${radiusText}m"
    else
        "${radiusText}m"
}

@Composable
fun RangeRings(
    rangeRingConfigs: List<Double>,
    centerPoint: Point
) {
    rangeRingConfigs.forEachIndexed { index, radiusInMeters ->

        // Skip ring placement if no data available.
        if (radiusInMeters <= 0.0) return@forEachIndexed

        // Create a circular polygon representing this ring.
        val ringPolygon = TurfTransformation.circle(
            centerPoint,
            radiusInMeters,
            360,
            TurfConstants.UNIT_METERS
        )

        // Draw the ring as a polyline.
        PolylineAnnotation(
            points = ringPolygon.coordinates()[0]
        ) {
            lineColor = Color.Black
            lineOpacity = 1.0
            lineWidth = 2.0
        }

        // Range Ring Annotation
        MapFeatureAnnotation(
            centerPoint,
            -radiusInMeters,
            formatRangeRingLabel(index, radiusInMeters)
        )    }
}