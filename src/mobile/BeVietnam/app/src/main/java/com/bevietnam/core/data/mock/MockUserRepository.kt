package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockUserRepository @Inject constructor() : IUserRepository {

    override fun getUser(id: String): Flow<Result<User>> = flow {
        delay(1000)
        // Dùng chung MOCK_USER từ MockAuthRepository để đảm bảo dữ liệu nhất quán
        emit(Result.success(MockAuthRepository.MOCK_USER.copy(id = id)))
    }

    override fun updateUser(user: User): Flow<Result<Unit>> = flow {
        delay(1000)
        emit(Result.success(Unit))
    }
}
