package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.MapConfigDto
import com.bevietnam.core.model.MapConfig

fun MapConfigDto.toMapConfig(): MapConfig = MapConfig(
    enabled = enabled,
    styleUrl = styleUrl,
    initialLatitude = initialLatitude,
    initialLongitude = initialLongitude,
    initialZoom = initialZoom
)
