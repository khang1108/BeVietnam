package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến việc chụp ảnh và chia sẻ bài viết khám phá.
 *
 * Lớp trừu tượng này giúp phân tách hoàn toàn phần logic đăng bài viết ở tầng Domain khỏi các chi tiết
 * truyền tải mạng hay lưu trữ nội bộ ở tầng Data.
 */
interface ICaptureRepository {
    
    /**
     * Thực hiện tải lên một bài viết khám phá mới cùng với ảnh chụp và thông tin vị trí địa lý đi kèm.
     *
     * @param metadata Chứa toàn bộ thông tin chi tiết của bài đăng bao gồm đường dẫn ảnh, vị trí tọa độ GPS và mô tả bài viết ([CaptureMetadata]).
     * @return Một [Flow] phát ra các trạng thái tải lên [Resource] (Loading, Success, Error) giúp UI cập nhật trạng thái tương ứng.
     */
    fun uploadCapture(metadata: CaptureMetadata, userId: String, placeId: String, taskId: String?): Flow<Resource<Unit>>
}
