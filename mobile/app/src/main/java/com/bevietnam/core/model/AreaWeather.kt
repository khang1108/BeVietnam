package com.bevietnam.core.model

/** Điều kiện thời tiết khu vực quanh người dùng, để hiển thị chip trên bản đồ. */
data class AreaWeather(
    val condition: String,
    val temp: Double? = null,
    val uvi: Double? = null,
    val rainMm: Double? = null,
    val clouds: Int? = null
)
