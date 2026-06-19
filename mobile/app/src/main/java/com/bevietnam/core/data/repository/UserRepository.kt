package com.bevietnam.core.data.repository

import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.mapper.toUser
import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: BeVietnamApi
) : IUserRepository {

    override fun getUser(id: String): Flow<Result<User>> = flow {
        try {
            val dto = api.getMe()
            emit(Result.success(dto.toUser()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateUser(user: User): Flow<Result<Unit>> {
        TODO("Not yet implemented: BE chưa có endpoint cập nhật user")
    }
}
