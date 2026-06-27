package com.bevietnam.core.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.api.dto.CaptureDetailsDto
import com.bevietnam.core.data.remote.api.dto.VerifyTaskCaptureBodyDto
import com.bevietnam.core.data.remote.mapper.toStorylineTaskDto
import com.bevietnam.core.domain.repository.ICaptureRepository
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: BeVietnamApi,
    private val taskRepository: ITaskRepository
) : ICaptureRepository {

    override fun uploadCapture(
        metadata: CaptureMetadata,
        userId: String,
        placeId: String,
        taskId: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            if (taskId == null) {
                emit(Resource.Error("Thiếu thông tin nhiệm vụ (taskId)"))
                return@flow
            }

            val task = taskRepository.getTaskById(taskId).firstOrNull()
            if (task == null) {
                emit(Resource.Error("Nhiệm vụ không tồn tại"))
                return@flow
            }

            // Convert image URI to Base64 data URL
            val base64DataUrl = getBase64DataUrl(metadata.mediaUrl)
            if (base64DataUrl == null) {
                emit(Resource.Error("Không thể đọc ảnh chụp"))
                return@flow
            }

            val verifyBody = VerifyTaskCaptureBodyDto(
                userId = userId,
                task = task.toStorylineTaskDto(),
                capture = CaptureDetailsDto(
                    mediaUrl = base64DataUrl,
                    note = metadata.note ?: "",
                    placeId = placeId,
                    latitude = metadata.latitude,
                    longitude = metadata.longitude
                )
            )

            val response = api.verifyCapture(verifyBody)
            if (response.approved) {
                // Complete task locally
                taskRepository.completeTask(taskId, metadata.mediaUrl, metadata.note)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.reason.ifBlank { "Minh chứng không được chấp nhận bởi AI Judge" }))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi xác minh minh chứng"))
        }
    }

    private fun getBase64DataUrl(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64String"
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
