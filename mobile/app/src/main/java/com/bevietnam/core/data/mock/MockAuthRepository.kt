package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IAuthRepository

import com.bevietnam.core.data.local.TokenStorage
import com.bevietnam.core.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor(
    private val tokenStorage: TokenStorage
) : IAuthRepository {

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        delay(1000)
        val mockUser = User(
            id = "mock-user-001",
            name = "Test User",

            email = email,
            avatarUrl = "https://ui-avatars.com/api/?name=Test+User&background=random"
        )
        // Lưu một token giả để test luồng Auto-login
        tokenStorage.saveToken("mock_jwt_token_12345")
        emit(Result.success(mockUser))
    }

    override fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Result<User>> = flow {
        delay(1000)
        val mockUser = User(
            id = "mock-user-002",
            name = name,
            email = email,
            avatarUrl = "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&background=random"
        )
        // Lưu một token giả để test luồng Auto-login sau khi đăng ký
        tokenStorage.saveToken("mock_jwt_token_12345")
        emit(Result.success(mockUser))
    }
}
