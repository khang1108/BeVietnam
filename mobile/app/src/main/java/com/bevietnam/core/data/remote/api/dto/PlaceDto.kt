package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("reference_url") val referenceUrl: String? = null
)
