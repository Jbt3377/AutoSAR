package com.example.autosar.compostables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mapbox.geojson.Point

/**
 * Dialog for adding a subject/marker and setting the 4th range ring radius.
 *
 * @param showForm controls whether the dialog is visible.
 * @param onDismiss called when the dialog is dismissed without confirmation.
 * @param onConfirm called with the marker point and user-entered radius when confirmed.
 */
@Composable
fun SubjectWizard(
    pendingPoint: Point,
    onDismiss: () -> Unit,
    onConfirm: (Point, Double?) -> Unit
) {
    var radiusInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add Range Rings", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = radiusInput,
                    onValueChange = { radiusInput = it },
                    label = { Text("Outer ring radius (m)") },
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                pendingPoint,
                                radiusInput.toDoubleOrNull()
                            )
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
