package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeedResponseDto(
    val items: List<FeedItemDto>
)
