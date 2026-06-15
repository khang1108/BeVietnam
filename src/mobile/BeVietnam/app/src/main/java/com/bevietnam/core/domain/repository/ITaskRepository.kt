package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến Nhiệm vụ trò chơi hóa (Gamified Tasks).
 *
 * Cung cấp giải pháp trừu tượng để quản lý hành trình nhiệm vụ khám phá văn hóa của người dùng,
 * bao gồm việc lấy danh sách nhiệm vụ, xác định nhiệm vụ tiếp theo, và ghi nhận hoàn thành nhiệm vụ.
 */
interface ITaskRepository {
    
    /**
     * Lấy danh sách toàn bộ các nhiệm vụ khám phá lịch sử, văn hóa hiện có trên hệ thống ứng dụng.
     *
     * @return Một [Flow] phát ra danh sách các nhiệm vụ [Task].
     */
    fun getTasks(): Flow<List<Task>>

    /**
     * Lấy thông tin nhiệm vụ tiếp theo mà người dùng cần thực hiện trong chuỗi nhiệm vụ cốt truyện.
     *
     * @return Một [Flow] phát ra đối tượng nhiệm vụ tiếp theo [Task] nếu có, hoặc `null` nếu đã hoàn thành tất cả.
     */
    fun getNextTask(): Flow<Task?>

    /**
     * Ghi nhận trạng thái hoàn thành đối với một nhiệm vụ cụ thể dựa trên mã định danh của nhiệm vụ đó.
     *
     * Phương thức này được khai báo dưới dạng `suspend` để tối ưu hóa hiệu suất chạy nền
     * và đồng bộ an toàn trạng thái hoàn thành của nhiệm vụ.
     *
     * @param taskId Mã định danh duy nhất của nhiệm vụ cần ghi nhận hoàn thành.
     */
    suspend fun completeTask(taskId: String)
}
