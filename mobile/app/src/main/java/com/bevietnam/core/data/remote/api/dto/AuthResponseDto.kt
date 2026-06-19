package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("user") val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("joined_date") val joinedDate: String? = null,
    @SerialName("level") val level: Int? = null,
    @SerialName("points") val points: Int? = null
)
