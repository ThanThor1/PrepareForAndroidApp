package com.example.testmapbox.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCard(
    modifier: Modifier = Modifier,
    searchQuery: String,
    isSearching: Boolean,
    searchError: String?,
    showStyleMenu: Boolean,
    mapStyles: List<Pair<String, String>>,
    onSearchQueryChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onSearchSubmit: () -> Unit,
    onToggleStyleMenu: () -> Unit,
    onStyleSelected: (String) -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Map",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mapbox Viewer",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                IconButton(onClick = onToggleStyleMenu) {
                    Icon(Icons.Default.Menu, "Styles")
                }
            }

            // Search Bar
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm địa điểm...") },
                leadingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Row {
                            IconButton(onClick = onSearchClear) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                            IconButton(onClick = onSearchSubmit) {
                                Icon(Icons.Default.Send, "Search")
                            }
                        }
                    }
                },
                singleLine = true,
                isError = searchError != null,
                supportingText = searchError?.let { { Text(it) } }
            )

            // Style Menu
            if (showStyleMenu) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    mapStyles.forEach { (name, style) ->
                        TextButton(
                            onClick = { onStyleSelected(style) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}