package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho siêu dữ liệu ảnh chụp khám phá của khách du lịch (Capture Metadata).
 *
 * Khớp với các field capture-level trong Backend schema `CaptureCreateRequest`.
 * Các field nghiệp vụ (userId, taskId, placeId) được truyền riêng qua Repository/UseCase.
 *
 * @property imageUrl Đường dẫn cục bộ (Uri string) hoặc đường dẫn máy chủ của hình ảnh đã chụp.
 * @property latitude Vĩ độ vị trí GPS khi chụp ảnh (có thể null nếu chưa có quyền vị trí).
 * @property longitude Kinh độ vị trí GPS khi chụp ảnh (có thể null nếu chưa có quyền vị trí).
 * @property note Ghi chú, cảm nhận ngắn về địa điểm hoặc nội dung chụp (có thể null).
 */
data class CaptureMetadata(
    val imageUrl: String,
    val latitude: Double?,
    val longitude: Double?,
    val note: String? = null
)

