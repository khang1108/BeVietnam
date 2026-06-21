package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm thực thi nghiệp vụ truy xuất chi tiết một nhiệm vụ cụ thể.
 *
 * Dùng cho màn hình TaskDetailScreen khi người dùng nhấn vào thẻ nhiệm vụ
 * để xem thông tin chi tiết đầy đủ.
 *
 * @property repository Hợp đồng Repository quản lý nhiệm vụ ([ITaskRepository]).
 */
class GetTaskDetailUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    /**
     * Kích hoạt nghiệp vụ lấy thông tin chi tiết nhiệm vụ theo mã định danh.
     *
     * @param taskId Mã định danh duy nhất của nhiệm vụ cần xem chi tiết.
     * @return Một [Flow] phát ra đối tượng [Task] nếu tìm thấy, hoặc `null`.
     */
    operator fun invoke(taskId: String): Flow<Task?> = repository.getTaskById(taskId)
}
