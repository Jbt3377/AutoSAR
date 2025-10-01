package com.example.autosar.composables.buttons

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.autosar.data.dtos.SubjectProfile
import com.example.autosar.data.repositories.LPBRepository
import com.example.autosar.services.exportGeoJsonService
import com.mapbox.geojson.Point

@Composable
fun ExportDialog(
    context: Context,
    markers: List<Point>,
    subjectProfile: SubjectProfile?,
    lpbRepository: LPBRepository,
    onDismiss: () -> Unit,
    onExported: () -> Unit
) {
    var exportFileName by remember { mutableStateOf("autosar_export") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export GeoJSON", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text("Enter a file name for the export:", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = exportFileName,
                    onValueChange = { exportFileName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val uri = exportGeoJsonService(
                        context = context,
                        markers = markers,
                        rangeRings = subjectProfile?.let { lpbRepository.getRingRadii(it)?.ringRadii },
                        centerPoint = markers.firstOrNull(),
                        subjectProfile = subjectProfile?.toString(),
                        fileName = exportFileName
                    )

                    uri?.let {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, it)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export GeoJSON"))
                    }

                    onExported() // tell parent to close
                }
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
