package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    operator fun invoke(id: String): Flow<Result<User>> {
        return userRepository.getUser(id)
    }
}

