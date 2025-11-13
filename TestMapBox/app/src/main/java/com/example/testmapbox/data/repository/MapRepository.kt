package com.example.testmapbox.data.repository

import com.example.testmapbox.data.model.LocationResult
import com.example.testmapbox.data.service.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapRepository(
    private val geocodingService: GeocodingService
) {

    suspend fun searchLocation(query: String): Result<LocationResult> {
        return withContext(Dispatchers.IO) {
            if (query.isBlank()) {
                Result.failure(Exception("Vui lòng nhập từ khóa tìm kiếm"))
            } else {
                geocodingService.searchLocation(query)
            }
        }
    }
}