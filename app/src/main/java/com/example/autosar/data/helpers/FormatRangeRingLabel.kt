package com.example.autosar.data.helpers

import java.text.NumberFormat
import java.util.Locale

private val PERCENTAGE_LABELS = listOf("25%", "50%", "75%", "95%")

/**
 * Formats a label for a range ring, combining a percentage prefix with a distance in meters.
 *
 * @param index The index to get the percentage prefix from.
 * @param radiusInMeters The radius value to format.
 * @return A formatted string, e.g., "75%: 1,500m" or "2,000m".
 */
fun formatRangeRingLabel(index: Int, radiusInMeters: Double): String {
    val prefix = PERCENTAGE_LABELS.getOrNull(index).orEmpty()

    val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }
    val radiusText = numberFormat.format(radiusInMeters)

    return if (prefix.isNotEmpty()) {
        "$prefix: ${radiusText}m"
    } else {
        "${radiusText}m"
    }
}