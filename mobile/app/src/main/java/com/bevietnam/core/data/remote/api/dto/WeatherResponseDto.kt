package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Area weather from backend `GET /weather` (OpenWeather-backed). */
@Serializable
data class WeatherResponseDto(
    val condition: String,
    val temp: Double? = null,
    val source: String,
    val uvi: Double? = null,
    @SerialName("rain_mm") val rainMm: Double? = null,
    val clouds: Int? = null
)
