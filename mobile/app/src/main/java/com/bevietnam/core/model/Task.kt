package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho một nhiệm vụ văn hóa/thử thách du lịch dành cho khách du lịch.
 *
 * Khớp với Backend schema `StorylineTask` tại `GET /api/v1/storyline/next-task`.
 *
 * @property id Định danh duy nhất của nhiệm vụ.
 * @property title Tiêu đề của nhiệm vụ.
 * @property description Mô tả hành động khách du lịch cần thực hiện để hoàn thành thử thách.
 * @property culturalExplanation Lời giải thích, bối cảnh lịch sử và ý nghĩa văn hóa đằng sau thử thách này.
 * @property completionRequirement Điều kiện cần thiết để xác minh hoàn thành (ví dụ: chụp ảnh, check-in).
 * @property difficulty Mức độ khó của nhiệm vụ ([TaskDifficulty]).
 * @property placeId Định danh của địa điểm liên quan trực tiếp đến thử thách này (nếu có).
 * @property score Điểm phù hợp dạng Float từ 0.0 đến 1.0.
 * @property isCompleted Trạng thái nhiệm vụ đã được người dùng hoàn thành hay chưa.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val culturalExplanation: String,
    val completionRequirement: String,
    val difficulty: TaskDifficulty,
    val placeId: String? = null,
    val score: Float = 0f,
    val isCompleted: Boolean = false,
    val captureImageUrl: String? = null,
    val captureNote: String? = null,
    val status: TaskStatus = TaskStatus.LOCKED
)

/**
 * Các mức độ khó khác nhau của nhiệm vụ khám phá văn hóa.
 */
enum class TaskDifficulty {
    /** Mức độ Dễ */
    EASY,
    /** Mức độ Trung bình */
    MEDIUM,
    /** Mức độ Khó */
    HARD
}

/**
 * Các trạng thái khác nhau của nhiệm vụ trong chuỗi quest chain.
 * Khớp với backend schema QuestTask.status: "locked | active | completed".
 */
enum class TaskStatus {
    /** Nhiệm vụ chưa mở khóa — cần hoàn thành nhiệm vụ trước đó */
    LOCKED,
    /** Nhiệm vụ đang hoạt động — người dùng có thể check-in */
    ACTIVE,
    /** Nhiệm vụ đã hoàn thành */
    COMPLETED
}