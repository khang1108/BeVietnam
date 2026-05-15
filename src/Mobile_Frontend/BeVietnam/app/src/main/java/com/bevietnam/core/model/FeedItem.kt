package com.bevietnam.core.model

data class FeedItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val location: String? = null,
    val category: String? = "Travel"
)
