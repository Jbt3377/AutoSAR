package com.example.autosar.composables.subjectWizard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Snowmobile
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.autosar.composables.subjectWizard.models.Category
import com.example.autosar.composables.subjectWizard.models.WizardStep
import com.example.autosar.data.dtos.SubjectProfile
import com.example.autosar.data.repositories.LPBRepository
import com.mapbox.geojson.Point

@Composable
fun SubjectWizard(
    pendingPoint: Point,
    repository: LPBRepository,
    onDismiss: () -> Unit,
    onConfirm: (Point, SubjectProfile) -> Unit
) {
    // Retrieve LPB Category list
    val rawCategories = repository.categories()
    val categories: List<Category> = remember(rawCategories) {
        rawCategories.mapNotNull { mapStringToCategory(it) }
    }

    var step by remember { mutableStateOf(WizardStep.Category) }
    var priorityCategory by remember { mutableStateOf<Category?>(null) }
    var selectedActivity by remember { mutableStateOf<String?>(null) }
    var selectedTerrain by remember { mutableStateOf<String?>(null) }
    var selectedAreaType by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            when (step) {
                WizardStep.Category -> {
                    CategoryStep(
                        categories = categories,
                        onCancel = onDismiss,
                        onNext = { subject ->
                            priorityCategory = subject
                            step = WizardStep.Activity
                        }
                    )
                }
                WizardStep.Activity -> {
                    val activities = remember(priorityCategory) {
                        repository.activities(priorityCategory?.label ?: "")
                    }

                    ActivityStep(
                        subject = priorityCategory!!,
                        activities = activities,
                        onBack = { step = WizardStep.Category },
                        onCancel = onDismiss,
                        onNext = { activity ->
                            selectedActivity = activity
                            step = WizardStep.Terrain
                        }
                    )
                }
                WizardStep.Terrain -> {
                    val terrains = remember(priorityCategory, selectedActivity) {
                        repository.terrains(priorityCategory?.label ?: "", selectedActivity ?: "")
                    }

                    TerrainStep(
                        terrains = terrains,
                        onBack = { step = WizardStep.Activity },
                        onCancel = onDismiss,
                        onNext = { terrain ->
                            selectedTerrain = terrain
                            step = WizardStep.AreaType
                        }
                    )
                }
                WizardStep.AreaType -> {
                    val areaTypes = remember(priorityCategory, selectedActivity, selectedAreaType) {
                        repository.areaTypes(
                            priorityCategory?.label ?: "",
                            selectedActivity ?: "",
                            selectedTerrain ?: ""
                        )
                    }

                    AreaTypeStep(
                        areaTypes = areaTypes,
                        onBack = { step = WizardStep.Terrain },
                        onCancel = onDismiss,
                        onConfirm = { areaType ->
                            selectedAreaType = areaType
                            onConfirm(
                                pendingPoint,
                                SubjectProfile(
                                    subject = priorityCategory?.label ?: "",
                                    activity = selectedActivity ?: "",
                                    terrain = selectedTerrain ?: "",
                                    area = selectedAreaType ?: ""
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun mapStringToCategory(categoryName: String): Category? {
    return when (categoryName) {
        "Aircraft" -> Category("Aircraft", Icons.Default.Flight, 1)
        "Abduction" -> Category("Abduction", Icons.Default.People, 2)
        "Water" -> Category("Water", Icons.Default.WaterDrop, 3)
        "Wheel/Motorized" -> Category("Wheel/Motorized", Icons.Default.DirectionsCar, 4)
        "Mental State" -> Category("Mental State", Icons.Default.QuestionMark, 5)
        "Child" -> Category("Child", Icons.Default.Person, 6)
        "Outdoor Activity" -> Category("Outdoor Activity", Icons.Filled.Hiking, 7)
        "Snow Activity" -> Category("Snow Activity", Icons.Default.Snowmobile, 8)
        else -> null // Handle unknown categories if necessary
    }
}