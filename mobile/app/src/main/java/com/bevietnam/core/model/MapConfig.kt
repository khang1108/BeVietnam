package com.bevietnam.core.model

/** Backend-hosted map style config for Explore MapLibre rendering. */
data class MapConfig(
    val enabled: Boolean,
    val styleUrl: String? = null,
    val initialLatitude: Double = 16.047079,
    val initialLongitude: Double = 108.206230,
    val initialZoom: Double = 5.5
)
