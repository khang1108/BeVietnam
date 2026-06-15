package com.bevietnam.core.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String = "",
    val gender: Gender? = null,
    val dateOfBirth: String? = null,
    val location: String? = null,
    val joinedDate: String? = null,
    val level: Int = 1,
    val points: Int = 0,
)