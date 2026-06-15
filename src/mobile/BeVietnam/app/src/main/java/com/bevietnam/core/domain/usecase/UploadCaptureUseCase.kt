package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ICaptureRepository
import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm thực thi nghiệp vụ đăng tải bài viết khám phá cùng ảnh chụp và tọa độ GPS của người dùng.
 *
 * Là thành phần duy nhất điều phối luồng nghiệp vụ tải lên, đảm bảo cô lập hoàn toàn logic nghiệp vụ
 * ở tầng Domain với chi tiết lưu trữ mạng hay local ở tầng Data.
 *
 * @property repository Hợp đồng Repository quản lý việc chụp và tải lên ([ICaptureRepository]).
 */
class UploadCaptureUseCase @Inject constructor(
    private val repository: ICaptureRepository
) {
    
    /**
     * Kích hoạt nghiệp vụ tải lên hình ảnh cùng các dữ liệu mô tả và tọa độ địa lý.
     *
     * Phương thức sử dụng toán tử `invoke` để đối tượng UseCase có thể được gọi
     * trực tiếp từ CaptureViewModel một cách tinh gọn.
     *
     * @param metadata Chứa thông tin chi tiết bài viết đăng tải ([CaptureMetadata]).
     * @return Một [Flow] phát ra các trạng thái tải lên [Resource] (Loading, Success, Error).
     */
    operator fun invoke(metadata: CaptureMetadata, userId: Int, placeId: Int, taskId: String?): Flow<Resource<Unit>> {
        return repository.uploadCapture(metadata, userId, placeId, taskId)
    }
}
