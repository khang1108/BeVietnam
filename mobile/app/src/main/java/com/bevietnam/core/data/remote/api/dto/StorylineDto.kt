package com.bevietnam.core.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestTaskDto(
    @SerialName("quest_id") val questId: String = "",
    @SerialName("task_id") val taskId: String,
    @SerialName("step_index") val stepIndex: Int = 1,
    val title: String,
    val description: String,
    @SerialName("cultural_explanation") val culturalExplanation: String,
    @SerialName("completion_requirement") val completionRequirement: String,
    val difficulty: String = "easy",
    val status: String = "locked",
    @SerialName("place_id") val placeId: String? = null,
    @SerialName("capture_image_url") val captureImageUrl: String? = null,
    @SerialName("capture_note") val captureNote: String? = null
)

@Serializable
data class QuestChainResponseDto(
    @SerialName("quest_id") val questId: String,
    @SerialName("place_name") val placeName: String,
    @SerialName("total_tasks") val totalTasks: Int,
    @SerialName("current_step") val currentStep: Int = 1,
    val tasks: List<QuestTaskDto>
)

@Serializable
data class StorylineTaskDto(
    @SerialName("task_id") val taskId: String,
    val title: String,
    val description: String,
    @SerialName("cultural_explanation") val culturalExplanation: String,
    @SerialName("completion_requirement") val completionRequirement: String,
    val difficulty: String = "easy",
    @SerialName("place_id") val placeId: String? = null,
    val score: Float = 0f
)

@Serializable
data class StorylineNextTaskResponseDto(
    val task: StorylineTaskDto,
    @SerialName("ai_generated") val aiGenerated: Boolean,
    val fallback: Boolean = false
)

@Serializable
data class VerifyTaskCaptureBodyDto(
    @SerialName("user_id") val userId: String,
    val task: StorylineTaskDto,
    val capture: CaptureDetailsDto
)

@Serializable
data class CaptureDetailsDto(
    @SerialName("media_url") val mediaUrl: String,
    val note: String? = null,
    @SerialName("place_id") val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class VerifyTaskCaptureResponseDto(
    val approved: Boolean,
    val status: String,
    val reason: String = "",
    val confidence: Double = 0.0
)
