package com.bevietnam.ui.screens.storyline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetTasksUseCase
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StorylineUiState {
    object Loading : StorylineUiState()
    data class Success(
        val tasks: List<Task>,
        val nextTask: Task?
    ) : StorylineUiState()
    object Empty : StorylineUiState()
    data class Error(val message: String) : StorylineUiState()
}

@HiltViewModel
class StorylineViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val repository: ITaskRepository // Still needed for completeTask until we have a UseCase for it
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorylineUiState>(StorylineUiState.Loading)
    val uiState: StateFlow<StorylineUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = StorylineUiState.Loading
            delay(1000) // Simulate network delay
            try {
                getTasksUseCase().collect { tasks ->
                    if (tasks.isEmpty()) {
                        _uiState.value = StorylineUiState.Empty
                    } else {
                        _uiState.value = StorylineUiState.Success(
                            tasks = tasks,
                            nextTask = repository.getNextTask().firstOrNull()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = StorylineUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onTaskCompleted(taskId: String) {
        viewModelScope.launch {
            repository.completeTask(taskId)
            loadTasks()
        }
    }
}
