package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho một gợi ý địa điểm trong bảng tin gợi ý (Recommendation Feed).
 *
 * Khớp với Backend schema `FeedItem` tại `GET /api/v1/feed`.
 *
 * @property id Định danh duy nhất của gợi ý.
 * @property placeId Định danh địa điểm liên kết với gợi ý này.
 * @property name Tên địa điểm được gợi ý.
 * @property category Danh mục phân loại (ví dụ: "temple", "museum", "park").
 * @property thumbnailUrl Đường dẫn URL hình ảnh thu nhỏ đại diện (có thể null).
 * @property score Điểm phù hợp dạng Float từ 0.0 đến 1.0.
 * @property explanation Đoạn giải thích chi tiết tại sao địa điểm này được gợi ý.
 * @property createdAt Thời điểm tạo gợi ý (ISO 8601 string).
 */
data class RecommendationItem(
    val id: String,
    val placeId: String,
    val name: String,
    val category: String,
    val thumbnailUrl: String? = null,
    val score: Float,
    val explanation: String,
    val createdAt: String
)

