package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.UserDto
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User

fun UserDto.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    bio = bio ?: "",
    gender = gender?.let { runCatching { Gender.valueOf(it.uppercase()) }.getOrNull() },
    dateOfBirth = dateOfBirth,
    location = location,
    joinedDate = joinedDate,
    level = level ?: 1,
    points = points ?: 0
)
