package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    operator fun invoke(user: User): Flow<Result<Unit>> {
        return userRepository.updateUser(user)
    }
}
