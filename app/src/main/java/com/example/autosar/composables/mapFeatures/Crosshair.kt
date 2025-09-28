package com.example.autosar.composables.mapFeatures

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.autosar.R

@Composable
fun Crosshair(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.crosshair),
        contentDescription = "Map crosshair",
        tint = Color.Black,
        modifier = modifier
            .size(20.dp)
    )
}