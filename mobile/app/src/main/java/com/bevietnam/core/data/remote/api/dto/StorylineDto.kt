package com.bevietnam.core.data.remote.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Phản hồi của endpoint GET /api/v1/storyline/next-task (khớp StorylineNextTaskResponse của BE).
 */
data class StorylineNextTaskResponseDto(
    @SerializedName("task") val task: StorylineTaskDto,
    @SerializedName("ai_generated") val aiGenerated: Boolean,
    @SerializedName("fallback") val fallback: Boolean = false
)

/**
 * Nhiệm vụ văn hóa (khớp StorylineTask của BE).
 */
data class StorylineTaskDto(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("cultural_explanation") val culturalExplanation: String,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("completion_requirement") val completionRequirement: String,
    @SerializedName("place_id") val placeId: String? = null,
    @SerializedName("score") val score: Float = 0f
)
