package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    fun login(email: String, password: String): Flow<Result<User>>
    fun register(
        name: String,
        gender: Gender?,
        dateOfBirth: String?,
        email: String,
        password: String
    ): Flow<Result<User>>
}
