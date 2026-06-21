package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.QuestChain
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm thực thi nghiệp vụ truy xuất chuỗi nhiệm vụ hành trình (Quest Chain).
 *
 * Cung cấp toàn bộ dữ liệu quest chain cho màn hình Storyline Duolingo-style path UI,
 * bao gồm danh sách nhiệm vụ đã sắp xếp, tiến độ hoàn thành và thông tin hành trình.
 *
 * @property repository Hợp đồng Repository quản lý nhiệm vụ ([ITaskRepository]).
 */
class GetQuestChainUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    /**
     * Kích hoạt nghiệp vụ lấy chuỗi nhiệm vụ hành trình.
     *
     * @return Một [Flow] phát ra đối tượng [QuestChain] chứa danh sách nhiệm vụ có thứ tự.
     */
    operator fun invoke(): Flow<QuestChain> = repository.getQuestChain()
}
