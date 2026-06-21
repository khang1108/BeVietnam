package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [IUserRepository] dùng để chạy/demo UI khi backend chưa sẵn sàng.
 * Khi nối backend thật, đổi binding trong RepositoryModule sang UserRepository.
 */
@Singleton
class MockUserRepository @Inject constructor() : IUserRepository {

    override fun getUser(id: String): Flow<Result<User>> = flow {
        emit(
            Result.success(
                User(
                    id = id,
                    name = "Hoàng Phi",
                    email = "hoangphipay@gmail.com",
                    avatarUrl = null,
                    createdAt = "01/01/2024"
                )
            )
        )
    }

    override fun updateUser(user: User): Flow<Result<Unit>> = flow {
        emit(Result.success(Unit))
    }
}
