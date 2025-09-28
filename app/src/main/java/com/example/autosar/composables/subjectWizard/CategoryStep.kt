package com.example.autosar.composables.subjectWizard

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.autosar.composables.subjectWizard.models.Category

@Composable
fun CategoryStep(
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