package com.bevietnam.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: Any, val title: String? = null, val icon: ImageVector? = null) {
    object Explore : Screen(ExploreRoute, "Khám phá", Icons.Default.Explore)
    object Profile : Screen(ProfileRoute(), "Hồ sơ", Icons.Default.AccountCircle)
    object Storyline : Screen(StorylineRoute, "Hành trình", Icons.Default.Map)
    object Feed : Screen(FeedRoute, "Bảng tin", Icons.Default.Home)
    object Community : Screen(CommunityRoute, "Cộng đồng", Icons.Default.People)

    companion object {
        val bottomNavItems = listOf(Explore, Feed, Storyline, Community, Profile)
    }
}
