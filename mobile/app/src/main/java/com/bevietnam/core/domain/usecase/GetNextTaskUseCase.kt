package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ lấy nhiệm vụ/thử thách khám phá văn hóa tiếp theo chưa hoàn thành.
 *
 * Lớp này tuân thủ Clean Architecture bằng cách che giấu chi tiết của tầng Data
 * và cung cấp một luồng nghiệp vụ duy nhất cho tầng UI (ViewModel).
 *
 * @property repository Contract Repository quản lý dữ liệu nhiệm vụ ([ITaskRepository]).
 */
class GetNextTaskUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    /**
     * Kích hoạt nghiệp vụ lấy nhiệm vụ kế tiếp chưa hoàn thành dưới dạng luồng dữ liệu quan sát được.
     *
     * @return Một [Flow] phát ra đối tượng [Task] tiếp theo chưa hoàn thành, hoặc `null` nếu tất cả nhiệm vụ đã hoàn tất.
     */
    operator fun invoke(): Flow<Task?> {
        return repository.getNextTask()
    }
}
