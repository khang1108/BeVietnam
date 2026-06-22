package com.bevietnam.core.model

/**
 * Mô hình dữ liệu đại diện cho một chuỗi nhiệm vụ hành trình (Quest Chain).
 *
 * Khớp với Backend schema `QuestChainResponse` tại `GET /api/v1/storyline/quest-chain`.
 *
 * @property questId Định danh duy nhất của chuỗi nhiệm vụ.
 * @property title Tiêu đề hành trình (ví dụ: "Di Sản Việt Nam").
 * @property description Mô tả ngắn về hành trình.
 * @property totalTasks Tổng số nhiệm vụ trong chuỗi.
 * @property currentStep Bước hiện tại mà người dùng đang thực hiện (1-indexed).
 * @property tasks Danh sách các nhiệm vụ đã sắp xếp theo thứ tự.
 */
data class QuestChain(
    val questId: String,
    val title: String,
    val description: String,
    val totalTasks: Int,
    val currentStep: Int,
    val tasks: List<Task>
)
