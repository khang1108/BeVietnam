package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IUserRepository

import com.bevietnam.core.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockUserRepository @Inject constructor() : IUserRepository {

    override fun getUser(id: String): Flow<Result<User>> = flow {
        delay(800)
        val mockUser = User(
            id = id,
            name = "Trần Nhựt Khang",

            email = "khang@bevietnam.com",
            avatarUrl = "https://ui-avatars.com/api/?name=Khang+Tran&background=0D8ABC&color=fff"
        )
        emit(Result.success(mockUser))
    }

    override fun updateUser(user: User): Flow<Result<Unit>> = flow {
        delay(1000)
        emit(Result.success(Unit))
    }
}
