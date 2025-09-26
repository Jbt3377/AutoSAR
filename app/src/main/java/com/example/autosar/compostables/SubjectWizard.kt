package com.example.autosar.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        Category("Aircraft", Icons.Default.AccountBox, 12800.0, priority = 1),
        Category("Abduction", Icons.Default.AccountBox, 1000.0, priority = 2),
        Category("Water", Icons.Default.AccountBox, 2400.0, priority = 3),
        Category("Wheel/Motorized", Icons.Default.AccountBox, 5000.0, priority = 4),
        Category("Mental State", Icons.Default.AccountBox, 3000.0, priority = 5),
        Category("Child", Icons.Default.AccountBox, 1500.0, priority = 6),
        Category("Outdoor Activity", Icons.Default.AccountBox, 2000.0, priority = 7),
        Category("Snow Activity", Icons.Default.AccountBox, 2500.0, priority = 8)
    )

    // Track selected categories
    val selected = remember { mutableStateListOf<Category>() }

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

                // Grid 2 x 4
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.5f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SelectableCategoryButton(
                                        category = cat,
                                        isSelected = cat in selected,
                                        onToggle = {
                                            if (cat in selected) {
                                                selected.remove(cat)
                                            } else {
                                                selected.add(cat)
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // pick the highest-priority among selected
                            val best = selected.minByOrNull { it.priority }
                            onConfirm(pendingPoint, best?.defaultRadius)
                        },
                        enabled = selected.isNotEmpty()
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private data class Category(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val defaultRadius: Double?,
    val priority: Int
)


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
