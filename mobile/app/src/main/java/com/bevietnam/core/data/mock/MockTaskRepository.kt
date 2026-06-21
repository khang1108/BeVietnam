package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [ITaskRepository] cho màn Hành trình dạng chuỗi.
 *
 * Giữ trạng thái hoàn thành trong bộ nhớ: hoàn thành nhiệm vụ hiện tại sẽ mở khóa
 * nhiệm vụ kế tiếp. Khi nối backend thật, đổi binding sang TaskRepository.
 */
@Singleton
class MockTaskRepository @Inject constructor() : ITaskRepository {

    private val _tasks = MutableStateFlow(seedTasks())

    override fun getTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override fun getNextTask(): Flow<Task?> =
        _tasks.map { list -> list.firstOrNull { !it.isCompleted } }

    override suspend fun completeTask(taskId: String) {
        _tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isCompleted = true) else it }
        }
    }

    private fun seedTasks(): List<Task> = listOf(
        Task(
            id = "1",
            title = "Khám phá Văn Miếu",
            description = "Đến thăm trường đại học đầu tiên của Việt Nam.",
            culturalExplanation = "Văn Miếu - Quốc Tử Giám xây năm 1070, là nơi thờ Khổng Tử và đào tạo nhân tài suốt gần 700 năm.",
            completionRequirement = "Chụp ảnh tại Khuê Văn Các",
            difficulty = TaskDifficulty.EASY,
            placeId = "1",
            score = 0.92f,
            isCompleted = true
        ),
        Task(
            id = "2",
            title = "Học làm đèn lồng Hội An",
            description = "Tham gia buổi học làm đèn lồng truyền thống phố cổ.",
            culturalExplanation = "Đèn lồng Hội An có từ thế kỷ 16, là biểu tượng của phố cổ và mang ý nghĩa cầu may, sum vầy.",
            completionRequirement = "Chụp ảnh chiếc đèn lồng tự làm",
            difficulty = TaskDifficulty.EASY,
            placeId = "2",
            score = 0.88f,
            isCompleted = false
        ),
        Task(
            id = "3",
            title = "Hang động Tràng An",
            description = "Đi thuyền khám phá quần thể hang động Tràng An.",
            culturalExplanation = "Tràng An là Di sản Thế giới kép được UNESCO công nhận, nổi tiếng với hệ thống hang động kỳ vĩ.",
            completionRequirement = "Chụp ảnh trong hang động",
            difficulty = TaskDifficulty.MEDIUM,
            placeId = "4",
            score = 0.80f,
            isCompleted = false
        ),
        Task(
            id = "4",
            title = "Leo núi Bà Nà Hills",
            description = "Chinh phục Bà Nà Hills và Cầu Vàng.",
            culturalExplanation = "Bà Nà Hills là khu nghỉ dưỡng trên núi từ thời Pháp, nay nổi tiếng với Cầu Vàng được nâng bởi đôi bàn tay khổng lồ.",
            completionRequirement = "Chụp ảnh tại Cầu Vàng",
            difficulty = TaskDifficulty.HARD,
            placeId = null,
            score = 0.75f,
            isCompleted = false
        ),
        Task(
            id = "5",
            title = "Hoàng thành Huế",
            description = "Tham quan kinh thành triều Nguyễn bên sông Hương.",
            culturalExplanation = "Hoàng thành Huế là quần thể di tích cố đô, kinh đô của triều Nguyễn suốt 143 năm.",
            completionRequirement = "Chụp ảnh tại Ngọ Môn",
            difficulty = TaskDifficulty.MEDIUM,
            placeId = "3",
            score = 0.85f,
            isCompleted = false
        )
    )
}
