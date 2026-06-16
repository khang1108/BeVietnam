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
object CameraRoute
