package com.example.testmapbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmapbox.data.model.LocationResult
import com.example.testmapbox.data.repository.MapRepository
import com.mapbox.maps.Style
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val mapStyle: String = Style.MAPBOX_STREETS,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchResult: LocationResult? = null,
    val showStyleMenu: Boolean = false
)

class MapViewModel(
    private val repository: MapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val mapStyles = listOf(
        "Streets" to Style.MAPBOX_STREETS,
        "Outdoors" to Style.OUTDOORS,
        "Light" to Style.LIGHT,
        "Dark" to Style.DARK,
        "Satellite" to Style.SATELLITE,
        "Satellite Streets" to Style.SATELLITE_STREETS
    )

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchError = null) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchError = null,
                searchResult = null
            )
        }
    }

    fun toggleStyleMenu() {
        _uiState.update { it.copy(showStyleMenu = !it.showStyleMenu) }
    }

    fun changeMapStyle(style: String) {
        _uiState.update {
            it.copy(
                mapStyle = style,
                showStyleMenu = false
            )
        }
    }

    fun searchLocation() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null) }

            repository.searchLocation(query)
                .onSuccess { location ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchResult = location,
                            searchError = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchError = error.message ?: "Đã xảy ra lỗi",
                            searchResult = null
                        )
                    }
                }
        }
    }

    fun clearSearchResult() {
        _uiState.update { it.copy(searchResult = null) }
    }
}