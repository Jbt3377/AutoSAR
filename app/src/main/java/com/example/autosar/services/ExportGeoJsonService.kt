package com.example.autosar.services

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import java.io.File

fun exportGeoJsonService(
    context: Context,
    markers: List<Point>,
    rangeRings: List<Double>?,
    centerPoint: Point?,
    subjectProfile: String? = null
): Uri? {
    val features = mutableListOf<Feature>()

    // Add markers
    markers.forEachIndexed { index, point ->
        features.add(
            Feature.fromGeometry(point).apply {
                addStringProperty("name", "Marker ${index + 1}")
            }
        )
    }

    // Add range rings (as polygons)
    if (centerPoint != null && !rangeRings.isNullOrEmpty()) {
        rangeRings.forEachIndexed { index, radius ->
            if (radius > 0.0) {
                val circle = com.mapbox.turf.TurfTransformation.circle(
                    centerPoint, radius, 360, com.mapbox.turf.TurfConstants.UNIT_METERS
                )
                features.add(
                    Feature.fromGeometry(circle).apply {
                        addStringProperty("name", "Range Ring ${index + 1}")
                    }
                )
            }
        }
    }

    // Wrap as FeatureCollection
    val featureCollection = FeatureCollection.fromFeatures(features)

    // Save to cache dir
    val file = File(context.cacheDir, "map_export.geojson")
    file.writeText(featureCollection.toJson())

    // Return a shareable URI
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
