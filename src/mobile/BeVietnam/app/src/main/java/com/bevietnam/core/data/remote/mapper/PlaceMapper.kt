package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.PlaceDto
import com.bevietnam.core.model.Place

fun PlaceDto.toPlace(): Place {
    return Place(
        id = id,
        name = name,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        referenceUrl = referenceUrl
    )
}
