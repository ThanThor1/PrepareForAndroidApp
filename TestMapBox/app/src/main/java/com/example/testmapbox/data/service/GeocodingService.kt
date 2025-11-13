package com.example.testmapbox.data.service

import com.example.testmapbox.data.model.GeocodingResponse
import com.example.testmapbox.data.model.LocationResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.takeFrom

class GeocodingService(
    private val httpClient: HttpClient,
    private val accessToken: String
) {
    private val baseUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places"

    suspend fun searchLocation(query: String): Result<LocationResult> {
        return try {
            val response = httpClient.get {
                url {
                    takeFrom("$baseUrl/$query.json")
                    parameter("access_token", accessToken)
                    parameter("limit", 1)
                }
            }

            val geocodingResponse: GeocodingResponse = response.body()

            if (geocodingResponse.features.isEmpty()) {
                Result.failure(Exception("Không tìm thấy địa điểm"))
            } else {
                val feature = geocodingResponse.features.first()
                val location = LocationResult(
                    longitude = feature.center[0],
                    latitude = feature.center[1],
                    placeName = feature.place_name
                )
                Result.success(location)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}