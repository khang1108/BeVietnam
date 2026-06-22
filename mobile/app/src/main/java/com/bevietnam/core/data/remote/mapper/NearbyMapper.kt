package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.NearbyPlaceDto
import com.bevietnam.core.data.remote.api.dto.WeatherResponseDto
import com.bevietnam.core.model.AreaWeather
import com.bevietnam.core.model.NearbyPlace

fun NearbyPlaceDto.toNearbyPlace(): NearbyPlace = NearbyPlace(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    category = category,
    categoryLabel = categoryLabel,
    address = address,
    distanceMeters = distanceMeters
)

fun WeatherResponseDto.toAreaWeather(): AreaWeather = AreaWeather(
    condition = condition,
    temp = temp,
    uvi = uvi,
    rainMm = rainMm,
    clouds = clouds
)
