package com.example.testmapbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val type: String,
    val features: List<Feature>
)

@Serializable
data class Feature(
    val type: String,
    val properties: Properties,
    val geometry: Geometry,
    val center: List<Double>,
    val place_name: String,
    val text: String
)

@Serializable
data class Properties(
    val mapbox_id: String? = null
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

// Domain model để UI sử dụng
data class LocationResult(
    val longitude: Double,
    val latitude: Double,
    val placeName: String
)