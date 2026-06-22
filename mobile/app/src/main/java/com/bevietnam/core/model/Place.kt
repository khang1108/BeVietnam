package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho một địa điểm/danh lam thắng cảnh văn hóa (Place/Point of Interest).
 *
 * Khớp với Backend schema `PlaceSchema` tại `GET /api/v1/places`.
 *
 * @property id Định danh duy nhất của địa điểm.
 * @property name Tên của địa điểm.
 * @property category Danh mục phân loại địa điểm (ví dụ: "temple", "museum", "park", "district").
 * @property description Mô tả chi tiết hoặc giới thiệu về giá trị văn hóa, ý nghĩa của địa điểm.
 * @property latitude Vĩ độ tọa độ GPS của địa điểm.
 * @property longitude Kinh độ tọa độ GPS của địa điểm.
 * @property imageUrl Đường dẫn URL dẫn tới hình ảnh đại diện của địa điểm (có thể null).
 * @property referenceUrl Đường dẫn URL tham chiếu bên ngoài về địa điểm (có thể null).
 */
data class Place(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val referenceUrl: String? = null
)
