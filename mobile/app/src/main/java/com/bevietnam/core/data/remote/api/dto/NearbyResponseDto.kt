package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Live POI from Foursquare (via backend `GET /places/nearby`). */
@Serializable
data class NearbyPlaceDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    @SerialName("category_label") val categoryLabel: String,
    val address: String? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    val rating: Double? = null,
    val popularity: Double? = null
)

@Serializable
data class NearbyResponseDto(
    val total: Int,
    val items: List<NearbyPlaceDto>
)
