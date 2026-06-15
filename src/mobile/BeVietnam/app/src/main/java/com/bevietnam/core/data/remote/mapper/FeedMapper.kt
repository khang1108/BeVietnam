package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.FeedItemDto
import com.bevietnam.core.model.RecommendationItem

fun FeedItemDto.toRecommendationItem(): RecommendationItem {
    return RecommendationItem(
        id = id,
        placeId = placeId,
        name = name,
        category = category,
        thumbnailUrl = thumbnailUrl,
        score = score,
        explanation = explanation,
        createdAt = createdAt
    )
}
