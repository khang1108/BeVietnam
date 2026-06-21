package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.ICaptureRepository
import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [ICaptureRepository] dùng để chạy/demo UI khi backend chưa sẵn sàng.
 * Giả lập đăng bài thành công ngay. Khi nối backend thật, đổi binding sang CaptureRepository.
 */
@Singleton
class MockCaptureRepository @Inject constructor() : ICaptureRepository {

    override fun uploadCapture(
        metadata: CaptureMetadata,
        userId: String,
        placeId: String,
        taskId: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(Unit))
    }
}
