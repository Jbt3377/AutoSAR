package com.example.autosar.data.dtos


data class SubjectProfile(
    val subject: String,
    val activity: String,
    val terrain: String,
    val area: String,
) {
    constructor() : this(
        subject = "",
        activity = "",
        terrain = "",
        area = ""
    )
}