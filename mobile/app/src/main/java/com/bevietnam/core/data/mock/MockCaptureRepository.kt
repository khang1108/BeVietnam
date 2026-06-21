package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.ICaptureRepository
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockCaptureRepository @Inject constructor(
    private val taskRepository: ITaskRepository
) : ICaptureRepository {
    override fun uploadCapture(
        metadata: CaptureMetadata,
        userId: String,
        placeId: String,
        taskId: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        delay(1500) // Giả lập thời gian upload

        // Giả lập ghi nhận hoàn thành nhiệm vụ và lưu ảnh cục bộ
        if (taskId != null) {
            taskRepository.completeTask(taskId, metadata.mediaUrl, metadata.note)
        }

        emit(Resource.Success(Unit))
    }
}
