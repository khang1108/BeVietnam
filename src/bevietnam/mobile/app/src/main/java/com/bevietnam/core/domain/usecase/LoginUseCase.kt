package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Result<User>> {
        return authRepository.login(email, password)
    }
}
