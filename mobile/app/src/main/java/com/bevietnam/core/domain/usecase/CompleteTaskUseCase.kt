package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ hoàn thành một nhiệm vụ/thử thách khám phá văn hóa.
 *
 * Lớp này tuân thủ Clean Architecture bằng cách che giấu chi tiết của tầng Data
 * và cung cấp một luồng nghiệp vụ duy nhất cho tầng UI (ViewModel).
 *
 * @property repository Contract Repository quản lý dữ liệu nhiệm vụ ([ITaskRepository]).
 */
class CompleteTaskUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    /**
     * Kích hoạt nghiệp vụ đánh dấu một nhiệm vụ là đã hoàn thành.
     *
     * @param taskId Định danh duy nhất của nhiệm vụ cần hoàn thành.
     */
    suspend operator fun invoke(taskId: String) {
        repository.completeTask(taskId)
    }
}
