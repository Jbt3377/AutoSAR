package com.example.autosar.data.dtos


data class SubjectProfile(
    val subject: String,
    val activity: String,
    val terrain: String,
    val area: String,
) {
    override fun toString(): String {
        return buildString {
            appendLine("Activity: $activity")
            appendLine("Terrain: $terrain")
            append("Area Type: $area")
        }
    }
}