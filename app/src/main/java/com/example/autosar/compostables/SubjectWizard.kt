package com.example.autosar.compostables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Snowmobile
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mapbox.geojson.Point

/**
 * Dialog for selecting a subject profile and data relevant for map features.
 *
 * @param pendingPoint Map location where the IPP is placed.
 * @param onDismiss Called when the dialog is dismissed without confirmation.
 * @param onConfirm Called with the subject profile is determined.
 */
@Composable
fun SubjectWizard(
    pendingPoint: Point,
    onDismiss: () -> Unit,
    onConfirm: (Point, Double?) -> Unit
) {
    // Temporary subject-activity data
    val categories = listOf(
        Category(
            "Aircraft", Icons.Default.Flight, priority = 1,
            activities = listOf(
                Activity("Low-Altitude Flight", 12_800.0),
                Activity("Commercial Flight", 15_000.0)
            )
        ),
        Category(
            "Abduction", Icons.Default.People, priority = 2,
            activities = listOf(
                Activity("Vehicle Abduction", 1_100.0),
                Activity("On-Foot Abduction", 900.0)
            )
        ),
        Category(
            "Water", Icons.Default.WaterDrop, priority = 3,
            activities = listOf(
                Activity("Angler", 2_400.0),
                Activity("Boater", 3_000.0)
            )
        ),
        Category(
            "Wheel/Motorized", Icons.Default.DirectionsCar, priority = 4,
            activities = listOf(
                Activity("ATV Rider", 5_000.0),
                Activity("Off-road Driver", 4_000.0)
            )
        ),
        Category(
            "Mental State", Icons.Default.QuestionMark, priority = 5,
            activities = listOf(
                Activity("Despondent", 3_000.0),
                Activity("Confused", 2_500.0)
            )
        ),
        Category(
            "Child", Icons.Default.Person, priority = 6,
            activities = listOf(
                Activity("Toddler", 1_500.0),
                Activity("School-Age", 2_000.0)
            )
        ),
        Category(
            "Outdoor Activity", Icons.Default.PedalBike, priority = 7,
            activities = listOf(
                Activity("Abandoned Vehicle", 2_000.0),
                Activity("Angler", 1_800.0),
                Activity("Car Camper", 2_200.0),
                Activity("Caver", 1_600.0),
                Activity("Day Climber", 2_400.0)
            )
        ),
        Category(
            "Snow Activity", Icons.Default.Snowmobile, priority = 8,
            activities = listOf(
                Activity("Back-country Skier", 2_500.0),
                Activity("Snowboarder", 2_700.0)
            )
        )
    )

    // State tracking
    var step by remember { mutableStateOf(WizardStep.Category) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            when (step) {
                WizardStep.Category -> CategoryStep(
                    categories = categories,
                    onCancel = onDismiss,
                    onNext = { cat ->
                        selectedCategory = cat
                        step = WizardStep.Activity
                    }
                )

                WizardStep.Activity -> ActivityStep(
                    category = requireNotNull(selectedCategory),
                    onBack = { step = WizardStep.Category },
                    onCancel = onDismiss,
                    onConfirm = { act ->
                        selectedActivity = act
                        onConfirm(pendingPoint, act.defaultRadius)
                    }
                )
            }
        }
    }
}

// Compostable specific classes
private data class Category(
    val label: String,
    val icon: ImageVector,
    val priority: Int,
    val activities: List<Activity>
)

private data class Activity(
    val label: String,
    val defaultRadius: Double
)

private enum class WizardStep { Category, Activity }

// Sub-compostables
@Composable
private fun CategoryStep(
    categories: List<Category>,
    onCancel: () -> Unit,
    onNext: (Category) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Subject Category", style = MaterialTheme.typography.titleMedium)

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
                            Modifier
                                .weight(1f)
                                .aspectRatio(1.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            CategoryButton(cat) { onNext(cat) }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun ActivityStep(
    category: Category,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (Activity) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Select ${category.label} Activity", style = MaterialTheme.typography.titleMedium)

        category.activities.forEach { act ->
            ListItem(
                headlineContent = { Text(act.label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfirm(act) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun CategoryButton(category: Category, onSelect: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onSelect() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                category.icon,
                contentDescription = category.label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = category.label, style = MaterialTheme.typography.labelSmall)
    }
}