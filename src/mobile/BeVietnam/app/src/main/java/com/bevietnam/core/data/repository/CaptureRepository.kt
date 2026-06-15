package com.bevietnam.core.data.repository

import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.domain.repository.ICaptureRepository
import com.bevietnam.core.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// TODO(Backend): Inject BeVietnamApi and implement actual API calls
@Singleton
class CaptureRepository @Inject constructor(
) : ICaptureRepository {
    override fun uploadCapture(metadata: CaptureMetadata, userId: Int, placeId: Int, taskId: String?): Flow<Resource<Unit>> {
        TODO("Not yet implemented");
    }
}
