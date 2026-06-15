package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.HealthStatus
import kotlinx.coroutines.flow.Flow

interface IHealthRepository {
    fun checkHealth(): Flow<Result<HealthStatus>>
}
