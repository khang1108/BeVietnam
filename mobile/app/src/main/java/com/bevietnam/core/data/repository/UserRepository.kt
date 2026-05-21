package com.bevietnam.core.data.repository

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// TODO(Backend): Inject BeVietnamApi and implement actual API calls
@Singleton
class UserRepository @Inject constructor(
    // private val api: BeVietnamApi
) : IUserRepository {

    override fun getUser(id: String): Flow<Result<User>> {
        TODO("Not yet implemented: connect to GET /api/v1/users/{id}")
    }

    override fun updateUser(user: User): Flow<Result<Unit>> {
        TODO("Not yet implemented: connect to PUT /api/v1/users/{id}")
    }
}
