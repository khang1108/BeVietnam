package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockAuthRepository @Inject constructor() : IAuthRepository {

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        delay(1500)
        val normalizedEmail = email.trim().lowercase()
        val normalizedPassword = password.trim()
        if (normalizedEmail == "test@bevietnam.com" && normalizedPassword == "password") {
            emit(Result.success(MOCK_USER))
        } else {
            emit(Result.failure(Exception("Sai email hoặc mật khẩu.\nGợi ý: test@bevietnam.com / password")))
        }
    }

    override fun register(
        name: String,
        gender: Gender?,
        dateOfBirth: String?,
        email: String,
        password: String
    ): Flow<Result<User>> = flow {
        delay(1500)
        if (name.isBlank() || email.isBlank() || password.length < 8) {
            emit(Result.failure(Exception("Thông tin không hợp lệ")))
        } else {
            emit(Result.success(
                User(
                    id = "u_${System.currentTimeMillis()}",
                    name = name,
                    email = email,
                    avatarUrl = "https://i.pravatar.cc/150?u=${email.hashCode()}",
                    bio = "",
                    dateOfBirth = dateOfBirth,
                    gender = gender
                )
            ))
        }
    }

    companion object {
        val MOCK_USER = User(
            id = "u1",
            name = "Nguyễn Văn Test",
            email = "test@bevietnam.com",
            avatarUrl = "https://i.pravatar.cc/150?u=u1",
            bio = "Tôi là một người yêu thích khám phá vẻ đẹp của Việt Nam.",
            dateOfBirth = "14 tháng 10, 1992",
            gender = Gender.MALE
        )
    }
}

