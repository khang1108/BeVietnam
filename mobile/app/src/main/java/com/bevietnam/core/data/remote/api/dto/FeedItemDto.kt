package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedItemDto(
    val id: String,
    @SerialName("place_id") val placeId: String,
    val name: String,
    val category: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val score: Float,
    val explanation: String,
    @SerialName("created_at") val createdAt: String
)
