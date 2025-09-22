package com.example.autosar.compostables

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.autosar.R
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions


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

    ViewAnnotation(
        options = viewAnnotationOptions {
            geometry(centerPoint)
            annotationAnchor {
                anchor(ViewAnnotationAnchor.TOP)
            }
        }
    ) {
        Surface(
            color = Color.Transparent,
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            val textContent = "IPP"
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