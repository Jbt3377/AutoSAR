package com.example.autosar.data.repositories

import android.content.Context
import com.example.autosar.data.dtos.SubjectProfile
import com.example.autosar.data.dtos.SubjectProfileLPBData
import kotlinx.serialization.json.*

class LPBRepository(private val context: Context) {

    private val jsonTree: JsonObject by lazy {
        val raw = context.assets
            .open("lost-person-behaviour.json")
            .bufferedReader()
            .use { it.readText() }
        Json.parseToJsonElement(raw).jsonObject
    }

    /**
     * Retrieve ringRadii for a specific subject / activity / terrain / climate.
     * Returns null if any part of the path is missing or has no ringRadii array.
     */
    fun getRingRadii(subjectProfile: SubjectProfile): SubjectProfileLPBData? {
        val subject = subjectProfile.subject
        val activity = subjectProfile.activity
        val terrain = subjectProfile.terrain
        val area = subjectProfile.area

        val radiiArray = jsonTree[subject]
            ?.jsonObject?.get(activity)
            ?.jsonObject?.get(terrain)
            ?.jsonObject?.get(area)
            ?.jsonObject?.get("ringRadii")
            ?.jsonArray
            ?: return null

        val rings = radiiArray.mapNotNull { it.toString().toDoubleOrNull() }
        return SubjectProfileLPBData(subject, activity, terrain, area, rings)
    }

    /** Optional helpers for populating dropdowns in the UI */
    fun categories(): List<String> = jsonTree.keys.toList()

    fun activities(subject: String): List<String> =
        jsonTree[subject]?.jsonObject?.keys?.toList().orEmpty()

    fun terrains(subject: String, activity: String): List<String> =
        jsonTree[subject]
            ?.jsonObject?.get(activity)
            ?.jsonObject?.keys?.toList().orEmpty()

    fun areas(subject: String, activity: String, terrain: String): List<String> =
        jsonTree[subject]
            ?.jsonObject?.get(activity)
            ?.jsonObject?.get(terrain)
            ?.jsonObject?.keys?.toList().orEmpty()
}