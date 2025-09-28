package com.example.autosar.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Snowmobile
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var rawCategories = repository.categories()
    val categories: List<Category> = remember(rawCategories) {
        rawCategories.mapNotNull { mapStringToCategory(it) }
    }

    var step by remember { mutableStateOf(WizardStep.Category) }
    var priorityCategory by remember { mutableStateOf<Category?>(null) }
    var selectedActivity by remember { mutableStateOf<String?>(null) }

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
                        onConfirm = { activity ->
                            selectedActivity = activity
                            // For now terrain/area can be empty or chosen in later steps
                            onConfirm(
                                pendingPoint,
                                SubjectProfile(
                                    subject = priorityCategory?.label ?: "",
                                    activity = selectedActivity ?: "",
                                    terrain = "",
                                    area = ""
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

private enum class WizardStep { Category, Activity }

@Composable
private fun CategoryStep(
    categories: List<Category>,
    onCancel: () -> Unit,
    onNext: (Category) -> Unit
) {
    val selectedCategories = remember { mutableStateListOf<Category>() }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Subject Category", style = MaterialTheme.typography.titleMedium)

        // Grid 2 x 4
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            SelectableCategoryButton(
                                category = cat,
                                isSelected = cat in selectedCategories,
                                onToggle = {
                                    if (cat in selectedCategories) {
                                        selectedCategories.remove(cat)
                                    } else {
                                        selectedCategories.add(cat)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    // pick the highest-priority among selected
                    val best = selectedCategories.minByOrNull { it.priority }
                    if (best != null) {
                        onNext(best)
                    }
                },
                enabled = selectedCategories.isNotEmpty()
            ) {
                Text("Confirm")
            }
        }
    }
}

@Composable
private fun ActivityStep(
    subject: Category,
    activities: List<String>,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Select ${subject.label} Activity", style = MaterialTheme.typography.titleMedium)
        activities.forEach { act ->
            ListItem(
                headlineContent = { Text(act) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfirm(act) }
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun SelectableCategoryButton(
    category: Category,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                category.icon,
                contentDescription = category.label,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private data class Category(
    val label: String,
    val icon: ImageVector,
    val priority: Int,
)

private fun mapStringToCategory(categoryName: String): Category? {
    return when (categoryName) {
        "Aircraft" -> Category("Aircraft", Icons.Default.Flight, 1)
        "Abduction" -> Category("Abduction", Icons.Default.People, 2) // Example icon
        "Water" -> Category("Water", Icons.Default.WaterDrop, 3)
        "Wheel/Motorized" -> Category("Wheel/Motorized", Icons.Default.DirectionsCar, 4)
        "Mental State" -> Category("Mental State", Icons.Default.QuestionMark, 5) // Example icon
        "Child" -> Category("Child", Icons.Default.Person, 6)
        "Outdoor Activity" -> Category("Outdoor Activity", Icons.Filled.Hiking, 7)
        "Snow Activity" -> Category("Snow Activity", Icons.Default.Snowmobile, 8)
        else -> null // Handle unknown categories if necessary
    }
}