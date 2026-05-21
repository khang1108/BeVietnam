package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {
    fun getTasks(): Flow<List<Task>>
    fun getNextTask(): Flow<Task?>
    suspend fun completeTask(taskId: String)
}
