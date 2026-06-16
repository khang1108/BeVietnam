package com.bevietnam.core.util

import kotlin.math.*

/**
 * Bộ công cụ xử lý tính toán tọa độ địa lý GPS và khoảng cách trong dự án BeVietnam.
 */
object LocationUtils {
    
    private const val EARTH_RADIUS_METERS = 6371000.0
    
    /**
     * Tính khoảng cách giữa hai tọa độ GPS (kinh độ, vĩ độ) bằng công thức Haversine.
     *
     * @param lat1 Vĩ độ điểm thứ nhất.
     * @param lng1 Kinh độ điểm thứ nhất.
     * @param lat2 Vĩ độ điểm thứ hai.
     * @param lng2 Kinh độ điểm thứ hai.
     * @return Khoảng cách giữa hai điểm tính bằng mét (m).
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val latDistance = Math.toRadians(lat2 - lat1)
        val lngDistance = Math.toRadians(lng2 - lng1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lngDistance / 2) * sin(lngDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}
