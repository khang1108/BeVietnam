package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm thực thi nghiệp vụ truy xuất danh sách toàn bộ nhiệm vụ khám phá cốt truyện.
 *
 * Đóng vai trò là thành phần trung gian định nghĩa logic nghiệp vụ cốt lõi,
 * giúp che giấu cấu trúc lưu trữ dữ liệu của tầng Data khỏi ViewModel của màn hình.
 *
 * @property repository Hợp đồng Repository quản lý nhiệm vụ ([ITaskRepository]) dùng để lấy dữ liệu.
 */
class GetTasksUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    
    /**
     * Kích hoạt nghiệp vụ lấy danh sách nhiệm vụ khám phá văn hóa lịch sử của ứng dụng.
     *
     * Phương thức sử dụng toán tử `invoke` để đối tượng UseCase có thể được gọi
     * trực tiếp như một hàm từ phía UI ViewModel.
     *
     * @return Một [Flow] phát ra danh sách các nhiệm vụ khám phá ([Task]).
     */
    operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}
