package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.HealthResponseDto
import com.bevietnam.core.model.HealthStatus

/**
 * Chuyển đổi DTO từ tầng Network (API) sang Domain Model cho tầng UI.
 */
fun HealthResponseDto.toHealthStatus(): HealthStatus {
    return HealthStatus(
        status = this.status,
        version = this.version
    )
}
