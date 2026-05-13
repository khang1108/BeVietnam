package com.bevietnam.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object AuthRoute

@Serializable
data class ProfileRoute(val userId: String = "")

@Serializable
object ExploreRoute

@Serializable
object StorylineRoute

@Serializable
object FeedRoute

@Serializable
object CommunityRoute
