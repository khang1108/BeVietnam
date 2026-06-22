package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponseDto(
    val status: String,
    val version: String,
    val timestamp: String
)
