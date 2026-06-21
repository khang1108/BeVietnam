package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [IAuthRepository] dùng để chạy/demo UI khi backend chưa sẵn sàng.
 * Khi nối backend thật, đổi binding trong RepositoryModule sang AuthRepository.
 */
@Singleton
class MockAuthRepository @Inject constructor() : IAuthRepository {

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.success(mockUser(email = email)))
    }

    override fun register(name: String, email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.success(mockUser(name = name, email = email)))
    }

    private fun mockUser(name: String = "Hoàng Phi", email: String) = User(
        id = "1",
        name = name,
        email = email,
        avatarUrl = null,
        createdAt = "01/01/2024"
    )
}
