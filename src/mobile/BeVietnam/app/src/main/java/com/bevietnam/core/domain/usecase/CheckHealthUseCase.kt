package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IHealthRepository
import com.bevietnam.core.model.HealthStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckHealthUseCase @Inject constructor(
    private val healthRepository: IHealthRepository
) {
    operator fun invoke(): Flow<Result<HealthStatus>> {
        return healthRepository.checkHealth()
    }
}
