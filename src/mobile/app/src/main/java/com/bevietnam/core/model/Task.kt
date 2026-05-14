package com.bevietnam.core.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val culturalExplanation: String,
    val completionRequirement: String,
    val difficulty: TaskDifficulty,
    val relatedPlaceId: String? = null,
    val isCompleted: Boolean = false
)

enum class TaskDifficulty {
    EASY, MEDIUM, HARD
}