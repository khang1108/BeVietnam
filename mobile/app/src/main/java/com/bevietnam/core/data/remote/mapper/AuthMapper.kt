package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.UserDto
import com.bevietnam.core.model.User

fun UserDto.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt
)
