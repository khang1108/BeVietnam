package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.QuestTaskDto
import com.bevietnam.core.data.remote.api.dto.QuestChainResponseDto
import com.bevietnam.core.data.remote.api.dto.StorylineTaskDto
import com.bevietnam.core.model.QuestChain
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import com.bevietnam.core.model.TaskStatus

fun QuestTaskDto.toTask(): Task {
    val difficultyEnum = when (difficulty.lowercase()) {
        "easy" -> TaskDifficulty.EASY
        "medium" -> TaskDifficulty.MEDIUM
        "hard" -> TaskDifficulty.HARD
        else -> TaskDifficulty.EASY
    }
    val statusEnum = when (status.lowercase()) {
        "locked" -> TaskStatus.LOCKED
        "active" -> TaskStatus.ACTIVE
        "completed" -> TaskStatus.COMPLETED
        else -> TaskStatus.LOCKED
    }
    return Task(
        id = taskId,
        title = title,
        description = description,
        culturalExplanation = culturalExplanation,
        completionRequirement = completionRequirement,
        difficulty = difficultyEnum,
        placeId = placeId,
        score = 0f,
        isCompleted = statusEnum == TaskStatus.COMPLETED,
        captureImageUrl = captureImageUrl,
        captureNote = captureNote,
        status = statusEnum
    )
}

fun QuestChainResponseDto.toQuestChain(): QuestChain {
    return QuestChain(
        questId = questId,
        title = "Hành trình $placeName",
        description = "Khám phá vẻ đẹp tiềm ẩn và các câu chuyện văn hóa lịch sử tại $placeName.",
        totalTasks = totalTasks,
        currentStep = currentStep,
        tasks = tasks.map { it.toTask() }
    )
}

fun StorylineTaskDto.toTask(): Task {
    val difficultyEnum = when (difficulty.lowercase()) {
        "easy" -> TaskDifficulty.EASY
        "medium" -> TaskDifficulty.MEDIUM
        "hard" -> TaskDifficulty.HARD
        else -> TaskDifficulty.EASY
    }
    return Task(
        id = taskId,
        title = title,
        description = description,
        culturalExplanation = culturalExplanation,
        completionRequirement = completionRequirement,
        difficulty = difficultyEnum,
        placeId = placeId,
        score = score,
        isCompleted = false,
        status = TaskStatus.ACTIVE
    )
}

fun Task.toStorylineTaskDto(): StorylineTaskDto {
    return StorylineTaskDto(
        taskId = id,
        title = title,
        description = description,
        culturalExplanation = culturalExplanation,
        completionRequirement = completionRequirement,
        difficulty = difficulty.name.lowercase(),
        placeId = placeId,
        score = score
    )
}
