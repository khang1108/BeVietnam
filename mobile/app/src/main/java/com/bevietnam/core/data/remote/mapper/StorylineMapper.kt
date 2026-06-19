package com.bevietnam.core.data.remote.mapper

import com.bevietnam.core.data.remote.api.dto.StorylineTaskDto
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty

fun StorylineTaskDto.toTask(): Task = Task(
    id = taskId,
    title = title,
    description = description,
    culturalExplanation = culturalExplanation,
    completionRequirement = completionRequirement,
    difficulty = when (difficulty.lowercase()) {
        "easy" -> TaskDifficulty.EASY
        "hard" -> TaskDifficulty.HARD
        else -> TaskDifficulty.MEDIUM
    },
    placeId = placeId,
    score = score
)
