package com.bevietnam.core.util

/**
 * Lớp chứa các hằng số dùng chung cho toàn bộ dự án BeVietnam.
 */
object Constants {
    /**
     * Đường dẫn URL cơ sở (Base URL) cho các yêu cầu gọi API Backend.
     */
    const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    
    /**
     * Thời gian chờ tối đa (timeout) mặc định cho các cuộc gọi mạng (tính bằng giây).
     */
    const val NETWORK_TIMEOUT = 30L
}
