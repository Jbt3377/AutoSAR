package com.example.autosar.compostables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mapbox.geojson.Point

/**
 * Dialog for selecting a subject category and adding a marker.
 *
 * @param pendingPoint  Map location where the subject is placed.
 * @param onDismiss     Called when the dialog is dismissed without confirmation.
 * @param onConfirm     Called with the marker point and a suggested ring radius
 *                      when a category is chosen.
 */
@Composable
fun SubjectWizard(
    pendingPoint: Point,
    onDismiss: () -> Unit,
    onConfirm: (Point, Double?) -> Unit
) {
    // Define categories with optional default ring radius (metres)
    val categories = listOf(
        Category("Aircraft", Icons.Default.Build, 12800.0),
        Category("Abduction", Icons.Default.Build, 1000.0),
        Category("Water", Icons.Default.Build, 2400.0),
        Category("Wheel/Motorized", Icons.Default.Build, 5000.0),
        Category("Mental State", Icons.Default.Build, 3000.0),
        Category("Child", Icons.Default.Build, 1500.0),
        Category("Outdoor Activity", Icons.Default.Build, 2000.0),
        Category("Snow Activity", Icons.Default.Build, 2500.0)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select Subject Category",
                    style = MaterialTheme.typography.titleMedium
                )

                // Arrange the 8 icons in a grid (2 columns x 4 rows)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (cat in rowItems) {
                                CategoryButton(
                                    category = cat,
                                    onSelect = {
                                        onConfirm(pendingPoint, cat.defaultRadius)
                                    }
                                )
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

private data class Category(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val defaultRadius: Double?
)

@Composable
private fun CategoryButton(category: Category, onSelect: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onSelect) {
            Icon(
                category.icon,
                contentDescription = category.label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
