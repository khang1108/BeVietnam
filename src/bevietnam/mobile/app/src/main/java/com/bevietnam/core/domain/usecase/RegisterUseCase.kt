package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    operator fun invoke(
        name: String,
        gender: Gender?,
        dateOfBirth: String?,
        email: String,
        password: String
    ): Flow<Result<User>> = authRepository.register(name, gender, dateOfBirth, email, password)
}
