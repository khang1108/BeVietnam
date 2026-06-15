package com.bevietnam.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lớp niêm phong định nghĩa các màn hình chính (Main Screen) thuộc cấu trúc Bottom Navigation Bar của ứng dụng.
 *
 * Cung cấp giải pháp gom nhóm thông tin hiển thị bao gồm Route object, tiêu đề hiển thị dạng chuỗi văn bản,
 * và biểu tượng vectơ đại diện để vẽ thanh điều hướng dưới cùng một cách tự động và đồng bộ.
 *
 * @property route Đối tượng đại diện cho tuyến đường điều hướng của màn hình ([Any]).
 * @property title Tiêu đề hiển thị dạng chuỗi tiếng Việt của màn hình trên thanh điều hướng. Mặc định là `null`.
 * @property icon Biểu tượng vectơ hiển thị đại diện cho màn hình ([ImageVector]). Mặc định là `null`.
 */
sealed class Screen(val route: Any, val title: String? = null, val icon: ImageVector? = null) {
    
    /** Màn hình Khám phá địa điểm du lịch văn hóa */
    object Explore : Screen(ExploreRoute, "Khám phá", Icons.Default.Explore)
    
    /** Màn hình Hồ sơ cá nhân của người dùng */
    object Profile : Screen(ProfileRoute(), "Hồ sơ", Icons.Default.AccountCircle)
    
    /** Màn hình Hành trình cốt truyện & Nhiệm vụ */
    object Storyline : Screen(StorylineRoute, "Hành trình", Icons.Default.Map)
    
    /** Màn hình Bảng tin văn hóa */
    object Feed : Screen(FeedRoute, "Bảng tin", Icons.Default.Home)
    
    companion object {
        /**
         * Danh sách tĩnh chứa toàn bộ các màn hình chính hiển thị trên thanh Bottom Navigation Bar.
         *
         * Danh sách được sắp xếp theo đúng thứ tự hiển thị mong muốn từ trái qua phải.
         */
        val bottomNavItems = listOf(Explore, Feed, Storyline, Profile)
    }
}
