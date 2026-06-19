package com.bevietnam.core.data.repository

import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.mapper.toTask
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.domain.session.SessionManager
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val api: BeVietnamApi,
    private val sessionManager: SessionManager
) : ITaskRepository {

    private fun currentUserId(): String =
        sessionManager.currentUser.value?.id?.toString() ?: "guest"

    // BE hiện chỉ có GET /storyline/next-task (1 task tiếp theo), chưa có endpoint
    // lấy cả danh sách. Tạm trả task tiếp theo dưới dạng list 1 phần tử để UI hiển thị.
    override fun getTasks(): Flow<List<Task>> = flow {
        val response = api.getNextTask(currentUserId())
        emit(listOf(response.task.toTask()))
    }

    override fun getNextTask(): Flow<Task?> = flow {
        val response = api.getNextTask(currentUserId())
        emit(response.task.toTask())
    }

    // BE chưa có endpoint đánh dấu hoàn thành task.
    override suspend fun completeTask(taskId: String) {
        // TODO(Backend): chờ BE cung cấp POST /storyline/tasks/{id}/complete
    }
}
