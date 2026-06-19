package com.bevietnam.core.data.repository

import com.bevietnam.core.data.local.TokenStorage
import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.api.dto.LoginRequestDto
import com.bevietnam.core.data.remote.api.dto.RegisterRequestDto
import com.bevietnam.core.data.remote.mapper.toUser
import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: BeVietnamApi,
    private val tokenStorage: TokenStorage
) : IAuthRepository {

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val response = api.login(LoginRequestDto(email = email, password = password))
            tokenStorage.saveToken(response.accessToken)
            emit(Result.success(response.user.toUser()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun register(
        name: String,
        gender: Gender?,
        dateOfBirth: String?,
        email: String,
        password: String
    ): Flow<Result<User>> = flow {
        try {
            val response = api.register(
                RegisterRequestDto(
                    name = name,
                    gender = gender?.name?.lowercase(),
                    dateOfBirth = dateOfBirth,
                    email = email,
                    password = password
                )
            )
            tokenStorage.saveToken(response.accessToken)
            emit(Result.success(response.user.toUser()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
