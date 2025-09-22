package com.example.autosar.compostables

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
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
        val annotationPoint = TurfMeasurement.destination(
            centerPoint,
            -radiusInMeters,
            0.0,
            TurfConstants.UNIT_METERS
        )

        ViewAnnotation(
            options = viewAnnotationOptions {
                geometry(annotationPoint)
                annotationAnchor {
                    anchor(ViewAnnotationAnchor.TOP)
                }
            }
        ) {
            Surface(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                val textContent = radiusInMeters.toString() + "m"
                val textStyle = MaterialTheme.typography.bodySmall

                Box {
                    // Stroke Text (background layer)
                    Text(
                        text = textContent,
                        color = Color.White,
                        style = textStyle.merge(
                            TextStyle(
                                drawStyle = Stroke(
                                    width = 4f,
                                    join = StrokeJoin.Round
                                )
                            )
                        )
                    )
                    // Fill Text (foreground layer)
                    Text(
                        text = textContent,
                        color = Color.Black,
                        style = textStyle
                    )
                }
            }
        }
    }
}