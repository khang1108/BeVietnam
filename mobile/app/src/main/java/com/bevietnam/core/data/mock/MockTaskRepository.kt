package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.QuestChain
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import com.bevietnam.core.model.TaskStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock Repository cung cấp dữ liệu giả cho tính năng Storyline Quest Chain.
 *
 * Dùng để phát triển và test UI khi API backend chưa sẵn sàng.
 * Bao gồm 5 nhiệm vụ mock với các trạng thái đa dạng (COMPLETED, ACTIVE, LOCKED).
 */
@Singleton
class MockTaskRepository @Inject constructor() : ITaskRepository {

    private val _mockTasks = MutableStateFlow(listOf(
        Task(
            id = "task-001",
            title = "Thử thách Phở Hà Nội",
            description = "Tìm và thưởng thức một bát phở truyền thống tại một quán phở nổi tiếng ở phố cổ Hà Nội.",
            culturalExplanation = "Phở là món ăn quốc hồn quốc túy của Việt Nam, xuất hiện từ đầu thế kỷ 20 tại Nam Định và Hà Nội. Nước dùng được ninh từ xương bò trong nhiều giờ, kết hợp với các gia vị đặc trưng như quế, hồi, thảo quả tạo nên hương vị độc đáo không thể nhầm lẫn.",
            completionRequirement = "Chụp ảnh bát phở truyền thống của bạn tại quán.",
            difficulty = TaskDifficulty.EASY,
            placeId = "place-hanoi-pho",
            score = 0.9f,
            isCompleted = true,
            captureImageUrl = "https://images.unsplash.com/photo-1503764654157-72d979d9af2f?w=400",
            captureNote = "Phở ở đây ngon xỉu, nước dùng ngọt thanh, thịt bò mềm tan trong miệng. Lần đầu ăn phở Nam Định chuẩn vị thế này!",
            status = TaskStatus.COMPLETED
        ),
        Task(
            id = "task-002",
            title = "Khám phá Văn Miếu - Quốc Tử Giám",
            description = "Ghé thăm trường đại học đầu tiên của Việt Nam, nơi thờ Khổng Tử và các bậc hiền triết.",
            culturalExplanation = "Văn Miếu được xây dựng năm 1070 dưới triều Lý Thánh Tông, là trường đại học đầu tiên của Việt Nam. Quốc Tử Giám được thành lập năm 1076, đào tạo nhân tài cho đất nước suốt hơn 700 năm. 82 tấm bia Tiến sĩ tại đây đã được UNESCO công nhận là Di sản tư liệu thế giới.",
            completionRequirement = "Check-in tại cổng Văn Miếu và chụp ảnh Khuê Văn Các.",
            difficulty = TaskDifficulty.MEDIUM,
            placeId = "place-hanoi-vanmieu",
            score = 0.85f,
            isCompleted = true,
            captureImageUrl = "https://images.unsplash.com/photo-1580187707247-7680877aebf1?w=400",
            captureNote = "Góc chụp Khuê Văn Các buổi chiều tà. Không khí ở đây thật uy nghiêm và thanh tịnh.",
            status = TaskStatus.COMPLETED
        ),
        Task(
            id = "task-003",
            title = "Đi dạo Phố cổ 36 phố phường",
            description = "Khám phá và dạo quanh khu phố cổ Hà Nội với 36 phố phường mang tên các nghề thủ công truyền thống.",
            culturalExplanation = "Khu phố cổ Hà Nội hình thành từ thế kỷ 15, mỗi phố chuyên buôn bán một mặt hàng riêng: Hàng Đào bán vải, Hàng Bạc bán đồ trang sức, Hàng Mã bán đồ vàng mã. Kiến trúc nhà ống đặc trưng \"mặt tiền hẹp, chiều sâu dài\" phản ánh nét văn hóa buôn bán độc đáo của người Hà Nội xưa.",
            completionRequirement = "Chụp ảnh tại một con phố cổ đặc trưng.",
            difficulty = TaskDifficulty.MEDIUM,
            placeId = "place-hanoi-oldquarter",
            score = 0.75f,
            isCompleted = false,
            captureImageUrl = null,
            status = TaskStatus.ACTIVE
        ),
        Task(
            id = "task-004",
            title = "Tham quan Hoàng thành Thăng Long",
            description = "Khám phá di sản thế giới UNESCO — trung tâm quyền lực của nhiều triều đại phong kiến Việt Nam.",
            culturalExplanation = "Hoàng thành Thăng Long là quần thể di tích gắn liền với lịch sử kinh thành Thăng Long - Hà Nội, từ thành Đại La thời tiền Thăng Long qua thời kỳ Đinh - Lê, kéo dài đến thời Nguyễn. Đây là công trình kiến trúc đồ sộ, được UNESCO công nhận là Di sản Văn hóa Thế giới năm 2010.",
            completionRequirement = "Chụp ảnh tại Cột cờ Hà Nội hoặc Đoan Môn.",
            difficulty = TaskDifficulty.HARD,
            placeId = "place-hanoi-hoangthanhthanglong",
            score = 0.7f,
            isCompleted = false,
            captureImageUrl = null,
            status = TaskStatus.LOCKED
        ),
        Task(
            id = "task-005",
            title = "Thưởng thức Cà phê trứng Giảng",
            description = "Tìm và thưởng thức ly cà phê trứng nguyên bản tại quán Cà phê Giảng — nơi khai sinh ra loại đồ uống huyền thoại này.",
            culturalExplanation = "Cà phê trứng được ông Nguyễn Văn Giảng sáng tạo vào năm 1946, khi sữa tươi khan hiếm trong thời kỳ chiến tranh. Ông đã thay thế sữa bằng lòng đỏ trứng gà đánh bông, tạo nên thức uống béo ngậy, thơm lừng trở thành biểu tượng ẩm thực Hà Nội.",
            completionRequirement = "Chụp ảnh ly cà phê trứng tại quán Giảng.",
            difficulty = TaskDifficulty.EASY,
            placeId = "place-hanoi-cafegiang",
            score = 0.65f,
            isCompleted = false,
            captureImageUrl = null,
            status = TaskStatus.LOCKED
        )
    ))

    override suspend fun completeTask(taskId: String, captureImageUrl: String?, captureNote: String?) {
        delay(500)
        val currentTasks = _mockTasks.value.toMutableList()
        val index = currentTasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = currentTasks[index]
            currentTasks[index] = task.copy(
                isCompleted = true,
                status = TaskStatus.COMPLETED,
                captureImageUrl = captureImageUrl,
                captureNote = captureNote
            )
            // Unlock next task
            if (index + 1 < currentTasks.size) {
                currentTasks[index + 1] = currentTasks[index + 1].copy(status = TaskStatus.ACTIVE)
            }
            _mockTasks.value = currentTasks
        }
    }

    override fun getTasks(): Flow<List<Task>> = _mockTasks

    override fun getNextTask(): Flow<Task?> = _mockTasks.map { tasks -> 
        tasks.firstOrNull { it.status == TaskStatus.ACTIVE } 
    }

    override fun getQuestChain(): Flow<QuestChain> = _mockTasks.map { tasks ->
        QuestChain(
            questId = "quest-hanoi-001",
            title = "Di Sản Việt Nam",
            description = "Khám phá hàng ngàn di sản, văn hoá và vẻ đẹp tiềm ẩn của cuộc đời. Hãy cùng chúng tôi mở một cuộc chuyến sĩ gia.",
            totalTasks = tasks.size,
            currentStep = tasks.count { it.isCompleted },
            tasks = tasks
        )
    }

    override fun getTaskById(taskId: String): Flow<Task?> = _mockTasks.map { tasks -> 
        tasks.find { it.id == taskId } 
    }
}
