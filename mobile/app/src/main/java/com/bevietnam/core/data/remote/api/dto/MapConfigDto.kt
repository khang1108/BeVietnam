package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapConfigDto(
    val enabled: Boolean,
    @SerialName("style_url") val styleUrl: String? = null,
    @SerialName("initial_latitude") val initialLatitude: Double = 16.047079,
    @SerialName("initial_longitude") val initialLongitude: Double = 108.206230,
    @SerialName("initial_zoom") val initialZoom: Double = 5.5
)
