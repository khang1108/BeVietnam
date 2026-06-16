package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho một người dùng/khách du lịch trong hệ thống BeVietnam.
 *
 * @property id Định danh duy nhất của người dùng.
 * @property name Tên đầy đủ hoặc tên hiển thị của người dùng.
 * @property email Địa chỉ email của người dùng.
 * @property avatarUrl Đường dẫn URL ảnh đại diện của người dùng (nếu có).
 * @property bio Lời tự giới thiệu ngắn về bản thân người dùng.
 * @property gender Giới tính của người dùng ([Gender]).
 * @property dateOfBirth Ngày sinh nhật của người dùng (Định dạng: dd/MM/yyyy).
 * @property location Địa điểm hiện tại hoặc quê quán của người dùng.
 * @property joinedDate Ngày tham gia ứng dụng.
 * @property level Cấp độ tài khoản hiện tại của người dùng dựa trên điểm số (bắt đầu từ 1).
 * @property points Tổng số điểm tích lũy của người dùng thông qua việc hoàn thành các thử thách văn hóa.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String = "",
    val gender: Gender? = null,
    val dateOfBirth: String? = null,
    val location: String? = null,
    val createdAt: String? = null,
    val level: Int = 1,
    val points: Int = 0,
)