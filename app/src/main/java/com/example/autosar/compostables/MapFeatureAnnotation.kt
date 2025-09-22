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
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement


@Composable
fun MapFeatureAnnotation(point: Point, yOffset: Double, textContent: String) {
    val annotationPoint = TurfMeasurement.destination(
        point,
        yOffset,
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
            val textStyle = MaterialTheme.typography.bodySmall

            Box {
                // Background text with a stroke to create an outline effect.
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
                // Foreground text.
                Text(
                    text = textContent,
                    color = Color.Black,
                    style = textStyle
                )
            }
        }
    }
}