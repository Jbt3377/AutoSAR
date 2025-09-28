package com.example.autosar.data.dtos

data class SubjectProfileLPBData(
    val subject: String,
    val activity: String,
    val terrain: String,
    val climate: String,
    val ringRadii: List<Double>
)