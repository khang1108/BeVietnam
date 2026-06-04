package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: ITaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}
