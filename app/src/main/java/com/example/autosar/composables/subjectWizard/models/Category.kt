package com.example.autosar.composables.subjectWizard.models

import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val label: String,
    val icon: ImageVector,
    val priority: Int,
)