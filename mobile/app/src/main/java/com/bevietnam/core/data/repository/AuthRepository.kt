package com.bevietnam.core.data.repository

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// TODO(Backend): Inject BeVietnamApi and implement actual API calls
@Singleton
class AuthRepository @Inject constructor(
    // private val api: BeVietnamApi,
    // private val sessionManager: SessionManager
) : IAuthRepository {

    override fun login(email: String, password: String): Flow<Result<User>> {
        TODO("Not yet implemented: connect to POST /api/v1/auth/login")
    }

    override fun register(
        name: String,
        gender: Gender?,
        dateOfBirth: String?,
        email: String,
        password: String
    ): Flow<Result<User>> {
        TODO("Not yet implemented: connect to POST /api/v1/auth/register")
    }
}
