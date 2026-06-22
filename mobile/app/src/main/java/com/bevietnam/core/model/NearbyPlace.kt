package com.bevietnam.core.model

/**
 * Live "nearby" POI (cafe, homestay, di tích...) lấy realtime quanh vị trí người dùng.
 * Khác với [Place] (danh sách văn hóa tuyển chọn), đây là dữ liệu Foursquare động.
 */
data class NearbyPlace(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,       // coarse bucket: food | lodging | culture | history | nature | place
    val categoryLabel: String,  // Foursquare's display category
    val address: String? = null,
    val distanceMeters: Int? = null
)
