package com.bevietnam.core.data.repository

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// TODO(Backend): Inject BeVietnamApi and implement actual API calls
@Singleton
class TaskRepository @Inject constructor(
    // private val api: BeVietnamApi
) : ITaskRepository {

    override fun getTasks(): Flow<List<Task>> {
        TODO("Not yet implemented: connect to GET /api/v1/tasks")
    }

    override fun getNextTask(): Flow<Task?> {
        TODO("Not yet implemented: connect to GET /api/v1/tasks/next")
    }

    override suspend fun completeTask(taskId: String) {
        TODO("Not yet implemented: connect to POST /api/v1/tasks/{id}/complete")
    }
}
