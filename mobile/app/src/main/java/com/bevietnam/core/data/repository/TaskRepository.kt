package com.bevietnam.core.data.repository

import android.content.SharedPreferences
import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.mapper.toQuestChain
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.domain.session.SessionManager
import com.bevietnam.core.model.QuestChain
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val api: BeVietnamApi,
    private val sessionManager: SessionManager,
    private val prefs: SharedPreferences
) : ITaskRepository {

    override fun getTasks(): Flow<List<Task>> = getQuestChain().map { it.tasks }

    override fun getNextTask(): Flow<Task?> = getQuestChain().map { chain ->
        chain.tasks.firstOrNull { it.status == TaskStatus.ACTIVE }
    }

    override suspend fun completeTask(taskId: String, captureImageUrl: String?, captureNote: String?) {
        val userId = sessionManager.currentUser.value?.id ?: "demo-user"

        val completedKey = "storyline_completed_tasks_$userId"
        val currentCompleted = prefs.getStringSet(completedKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentCompleted.add(taskId)
        prefs.edit().putStringSet(completedKey, currentCompleted).apply()

        if (captureImageUrl != null) {
            prefs.edit().putString("storyline_task_image_${userId}_$taskId", captureImageUrl).apply()
        }
        if (captureNote != null) {
            prefs.edit().putString("storyline_task_note_${userId}_$taskId", captureNote).apply()
        }
    }

    override fun getQuestChain(): Flow<QuestChain> = flow {
        val userId = sessionManager.currentUser.value?.id ?: "demo-user"

        try {
            val response = api.getQuestChain(userId = userId)
            val domainQuest = response.toQuestChain()

            val completedKey = "storyline_completed_tasks_$userId"
            val completedTaskIds = prefs.getStringSet(completedKey, emptySet()) ?: emptySet()

            val updatedTasks = domainQuest.tasks.map { task ->
                val isCompleted = completedTaskIds.contains(task.id)
                val img = prefs.getString("storyline_task_image_${userId}_${task.id}", null) ?: task.captureImageUrl
                val note = prefs.getString("storyline_task_note_${userId}_${task.id}", null) ?: task.captureNote

                task.copy(
                    isCompleted = isCompleted,
                    captureImageUrl = img,
                    captureNote = note,
                    status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.LOCKED
                )
            }

            val firstActiveIdx = updatedTasks.indexOfFirst { it.status != TaskStatus.COMPLETED }
            val finalTasks = updatedTasks.mapIndexed { i, task ->
                when {
                    i == firstActiveIdx -> task.copy(status = TaskStatus.ACTIVE)
                    task.status != TaskStatus.COMPLETED -> task.copy(status = TaskStatus.LOCKED)
                    else -> task
                }
            }

            emit(QuestChain(
                questId = domainQuest.questId,
                title = domainQuest.title,
                description = domainQuest.description,
                totalTasks = domainQuest.totalTasks,
                currentStep = if (firstActiveIdx >= 0) firstActiveIdx + 1 else domainQuest.totalTasks + 1,
                tasks = finalTasks
            ))
        } catch (e: Exception) {
            // Fallback to local default / empty quest chain on network failure or if quest doesn't exist
            emit(QuestChain(
                questId = "quest-hue-imperial",
                title = "Hành trình Kinh thành Huế",
                description = "Khám phá vẻ đẹp Kinh thành Huế.",
                totalTasks = 0,
                currentStep = 1,
                tasks = emptyList()
            ))
        }
    }

    override fun getTaskById(taskId: String): Flow<Task?> = getQuestChain().map { chain ->
        chain.tasks.find { it.id == taskId }
    }
}
