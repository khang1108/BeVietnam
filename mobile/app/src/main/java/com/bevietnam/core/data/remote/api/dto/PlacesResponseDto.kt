package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlacesResponseDto(
    val total: Int,
    val items: List<PlaceDto>
)
