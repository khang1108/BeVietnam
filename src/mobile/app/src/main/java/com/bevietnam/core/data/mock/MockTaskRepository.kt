package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTaskRepository @Inject constructor() : ITaskRepository {

    private val _tasks = MutableStateFlow(
        listOf(
            Task(
                id = "1",
                title = "Thăm Chùa Một Cột",
                description = "Hãy đến thăm ngôi chùa cổ kính Một Cột tại Hà Nội và chụp một bức ảnh kỷ niệm tại đây.",
                culturalExplanation = "Chùa Một Cột được xây dựng năm 1049 dưới triều Lý Thái Tông, tượng trưng cho hoa sen nở trên mặt hồ — biểu tượng của sự thuần khiết trong Phật giáo Việt Nam.",
                completionRequirement = "Chụp ảnh tại chùa và chia sẻ lên ứng dụng",
                difficulty = TaskDifficulty.EASY,
                relatedPlaceId = "5"
            ),
            Task(
                id = "2",
                title = "Thử món Bánh Mì Hội An",
                description = "Tìm một tiệm bánh mì truyền thống tại Hội An và thưởng thức hương vị đặc trưng của ẩm thực miền Trung.",
                culturalExplanation = "Bánh mì Hội An là sự giao thoa văn hóa độc đáo giữa ẩm thực Pháp và Việt Nam, với nhân đặc trưng gồm pate, thịt xá xíu và rau thơm địa phương.",
                completionRequirement = "Ghé ít nhất 2 tiệm bánh mì và đánh giá trên ứng dụng",
                difficulty = TaskDifficulty.EASY,
                relatedPlaceId = "1"
            ),
            Task(
                id = "3",
                title = "Học làm đèn lồng Hội An",
                description = "Tham gia lớp học làm đèn lồng truyền thống tại một xưởng thủ công ở phố cổ Hội An.",
                culturalExplanation = "Nghề làm đèn lồng Hội An có lịch sử hơn 400 năm, là biểu tượng văn hóa phi vật thể quan trọng. Mỗi chiếc đèn lồng được làm thủ công với khung tre và vải lụa.",
                completionRequirement = "Hoàn thành 1 chiếc đèn lồng và chụp ảnh sản phẩm",
                difficulty = TaskDifficulty.MEDIUM,
                relatedPlaceId = "1"
            ),
            Task(
                id = "4",
                title = "Khám phá hang động Tràng An",
                description = "Chèo thuyền qua ít nhất 3 hang động trong quần thể Tràng An và ghi chép lại những điều thú vị.",
                culturalExplanation = "Tràng An là cái nôi của người Việt cổ với bằng chứng sinh sống từ 30.000 năm trước. Hệ thống hang động nơi đây từng là nơi trú ẩn và thờ cúng của người Việt thời tiền sử.",
                completionRequirement = "Hoàn thành tour thuyền qua 3 hang động, chụp ảnh tại mỗi hang",
                difficulty = TaskDifficulty.MEDIUM,
                relatedPlaceId = "3"
            ),
            Task(
                id = "5",
                title = "Leo núi Bà Nà Hills",
                description = "Chinh phục cung đường trekking lên đỉnh Bà Nà Hills và tận hưởng cảnh quan toàn cảnh Đà Nẵng từ trên cao.",
                culturalExplanation = "Bà Nà Hills không chỉ là khu nghỉ dưỡng hiện đại mà còn là nơi người Pháp xây dựng các biệt thự nghỉ dưỡng từ thế kỷ 20, tạo nên sự giao thoa kiến trúc Đông-Tây độc đáo.",
                completionRequirement = "Hoàn thành cung đường trekking 5km và check-in tại đỉnh núi",
                difficulty = TaskDifficulty.HARD,
                relatedPlaceId = null
            )
        )
    )

    override fun getTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override fun getNextTask(): Flow<Task?> = _tasks.map { tasks ->
        tasks.firstOrNull { !it.isCompleted }
    }

    override suspend fun completeTask(taskId: String) {
        val currentTasks = _tasks.value
        val updatedTasks = currentTasks.map {
            if (it.id == taskId) it.copy(isCompleted = true) else it
        }
        _tasks.value = updatedTasks
    }
}
