package com.example.testmapbox.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testmapbox.ui.viewmodel.MapViewModel
import com.example.testmapbox.ui.components.MapView
import com.example.testmapbox.ui.components.SearchCard
import com.example.testmapbox.ui.components.ZoomControls

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasLocationPermission by remember { mutableStateOf(false) }
    var mapViewCallback by remember { mutableStateOf<MapViewCallback?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Handle search result
    LaunchedEffect(uiState.searchResult) {
        uiState.searchResult?.let { location ->
            mapViewCallback?.animateToLocation(
                longitude = location.longitude,
                latitude = location.latitude
            )
            mapViewCallback?.addMarker(
                longitude = location.longitude,
                latitude = location.latitude
            )
            viewModel.clearSearchResult()
        }
    }

    // Handle map style change
    LaunchedEffect(uiState.mapStyle) {
        mapViewCallback?.changeMapStyle(uiState.mapStyle)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapView(
            modifier = Modifier.fillMaxSize(),
            hasLocationPermission = hasLocationPermission,
            onMapReady = { callback ->
                mapViewCallback = callback
            }
        )

        SearchCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            searchQuery = uiState.searchQuery,
            isSearching = uiState.isSearching,
            searchError = uiState.searchError,
            showStyleMenu = uiState.showStyleMenu,
            mapStyles = viewModel.mapStyles,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onSearchClear = viewModel::clearSearch,
            onSearchSubmit = viewModel::searchLocation,
            onToggleStyleMenu = viewModel::toggleStyleMenu,
            onStyleSelected = viewModel::changeMapStyle
        )

        ZoomControls(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp),
            onZoomIn = { mapViewCallback?.zoomIn() },
            onZoomOut = { mapViewCallback?.zoomOut() },
            onResetView = { mapViewCallback?.resetView() }
        )
    }
}

// Interface để giao tiếp với MapView
interface MapViewCallback {
    fun animateToLocation(longitude: Double, latitude: Double)
    fun addMarker(longitude: Double, latitude: Double)
    fun changeMapStyle(style: String)
    fun zoomIn()
    fun zoomOut()
    fun resetView()
}