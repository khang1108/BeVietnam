package com.bevietnam.core.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "Bearer",
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("joined_date") val joinedDate: String?,
    @SerializedName("level") val level: Int?,
    @SerializedName("points") val points: Int?
)
