package com.example.autosar.services

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.autosar.data.helpers.formatRangeRingLabel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation
import java.io.File

fun exportGeoJsonService(
    context: Context,
    markers: List<Point>,
    rangeRings: List<Double>?,
    centerPoint: Point?,
    subjectProfile: String? = null,
    fileName: String? = null,
): Uri? {
    val features = mutableListOf<Feature>()

    // Add markers as Caltopo-compatible points
    markers.forEachIndexed { index, point ->
        val feature = Feature.fromGeometry(point).apply {
            addStringProperty("title", if (index == 0) "IPP" else "Marker ${index + 1}")
            addStringProperty("class", "Marker")
            addStringProperty("marker-symbol", "point")
            addStringProperty("marker-color", "FF0000")
            addStringProperty("creator", "AutoSAR")
            addNumberProperty("weight", 1)
        }
        features.add(feature)
    }

    // Add range rings as LineStrings (Caltopo-style circles)
    if (centerPoint != null && !rangeRings.isNullOrEmpty()) {
        rangeRings.forEachIndexed { index, radius ->
            if (radius > 0.0) {
                // Generate circle polygon using Turf
                val circle = TurfTransformation.circle(
                    centerPoint,
                    radius,
                    64,
                    TurfConstants.UNIT_METERS
                )

                val lineString = LineString.fromLngLats(circle.coordinates()[0])
                val annotation = formatRangeRingLabel(index, radius)

                val feature = Feature.fromGeometry(lineString).apply {
                    addStringProperty("title", annotation)
                    addStringProperty("class", "Shape")
                    addStringProperty("stroke", "#000000")
                    addNumberProperty("stroke-opacity", 1)
                    addNumberProperty("weight", 2)
                    addStringProperty("creator", "AutoSAR")
                }
                features.add(feature)
            }
        }
    }

    // Aggregate file values
    val featureCollection = FeatureCollection.fromFeatures(features)
    val validFileName = validateFileName(fileName)

    // Write to file
    val file = File(context.cacheDir, "${validFileName}.json")
    file.writeText(featureCollection.toJson())

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun validateFileName(fileName: String?): String {
    return if (fileName.isNullOrEmpty()) "autosar_export" else fileName
}
