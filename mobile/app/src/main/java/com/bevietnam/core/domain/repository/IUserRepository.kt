package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getUser(id: String): Flow<Result<User>>
    fun updateUser(user: User): Flow<Result<Unit>>
}
