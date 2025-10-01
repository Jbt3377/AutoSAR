package com.example.autosar.services

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.autosar.data.helpers.formatRangeRingLabel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation

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
            addStringProperty("description", subjectProfile)
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

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "${validFileName}.json")
        put(MediaStore.Downloads.MIME_TYPE, "application/json")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { out ->
            out.write(featureCollection.toJson().toByteArray())
        }
    }

    return uri
}

fun validateFileName(fileName: String?): String {
    return if (fileName.isNullOrEmpty()) "autosar_export" else fileName
}
