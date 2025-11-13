package com.example.testmapbox.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ZoomControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit
) {
    Column(modifier = modifier) {
        FloatingActionButton(
            onClick = onZoomIn,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Add, "Zoom In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        FloatingActionButton(
            onClick = onZoomOut,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Remove, "Zoom Out")
        }

        Spacer(modifier = Modifier.height(8.dp))

        FloatingActionButton(
            onClick = onResetView,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(Icons.Default.Home, "Reset View")
        }
    }
}