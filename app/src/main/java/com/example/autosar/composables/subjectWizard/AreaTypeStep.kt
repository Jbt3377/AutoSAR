package com.example.autosar.composables.subjectWizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AreaTypeStep(
    areaTypes: List<String>,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Select Area Type", style = MaterialTheme.typography.titleMedium)
        areaTypes.forEach { at ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfirm(at) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = at,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterStart)
                )
            }
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