package com.bevietnam.core.data.repository

import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.domain.repository.IHealthRepository
import com.bevietnam.core.model.HealthStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

import com.bevietnam.core.data.remote.mapper.toHealthStatus

@Singleton
class HealthRepository @Inject constructor(
    private val api: BeVietnamApi
) : IHealthRepository {

    override fun checkHealth(): Flow<Result<HealthStatus>> = flow {
        try {
            val response = api.checkHealth()
            val healthStatus = response.toHealthStatus()
            emit(Result.success(healthStatus))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
