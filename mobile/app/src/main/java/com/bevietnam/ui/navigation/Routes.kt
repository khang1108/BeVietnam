package com.bevietnam.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Tuyến đường điều hướng đến màn hình Xác thực tài khoản (Auth Screen).
 *
 * Màn hình này bao gồm các chức năng đăng nhập và đăng ký tài khoản mới.
 */
@Serializable
object AuthRoute

/**
 * Tuyến đường điều hướng đến màn hình Hồ sơ cá nhân (Profile Screen).
 *
 * Yêu cầu truyền kèm mã định danh duy nhất của người dùng cần hiển thị thông tin.
 *
 * @property userId Định danh duy nhất của người dùng (User ID) cần hiển thị hồ sơ. Mặc định là -1.
 */
@Serializable
data class ProfileRoute(val userId: String = "-1")

/**
 * Tuyến đường điều hướng đến màn hình Khám phá (Explore Screen).
 *
 * Hiển thị bản đồ và danh sách các địa danh du lịch lịch sử, văn hóa tại Việt Nam.
 */
@Serializable
object ExploreRoute

@Serializable
data class PlaceDetailRoute(val placeId: String)

/**
 * Tuyến đường điều hướng đến màn hình Cốt truyện & Nhiệm vụ (Storyline Screen).
 *
 * Cung cấp hành trình trò chơi hóa (gamification) tìm hiểu văn hóa Việt Nam của người dùng.
 */
@Serializable
object StorylineRoute

/**
 * Tuyến đường điều hướng đến màn hình Bảng tin văn hóa (Feed Screen).
 *
 * Nơi hiển thị các bài viết chia sẻ trải nghiệm du lịch văn hóa từ cộng đồng người dùng.
 */
@Serializable
object FeedRoute



/**
 * Tuyến đường điều hướng đến màn hình Máy ảnh & Chia sẻ (Capture/Camera Screen).
 *
 * Cho phép người dùng chụp ảnh và đăng bài viết cảm nghĩ kèm tọa độ định vị GPS thực tế.
 */
@Serializable
data class CameraRoute(
    val placeId: String? = null,
    val taskId: String? = null
)

/**
 * Tuyến đường điều hướng đến màn hình Chi tiết Nhiệm vụ (Task Detail Screen).
 *
 * Hiển thị đầy đủ thông tin nhiệm vụ bao gồm ảnh đã chụp, giải thích văn hóa và nút check-in.
 *
 * @property taskId Định danh duy nhất của nhiệm vụ cần xem chi tiết.
 */
@Serializable
data class TaskDetailRoute(val taskId: String)

