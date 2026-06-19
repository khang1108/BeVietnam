package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RegisterRequestDto(
    @SerialName("name") val name: String,
    @SerialName("gender") val gender: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)
