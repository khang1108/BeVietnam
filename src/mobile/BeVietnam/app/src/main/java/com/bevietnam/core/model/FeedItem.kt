package com.bevietnam.core.model

data class FeedItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: Any, // -> để dùng được drawableee
    val content: String,
    val imageUrl: Any? = null, 
    val timestamp: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val location: String? = null,
    val category: String? = "Travel"
)
