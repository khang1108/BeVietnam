package com.bevietnam.ui.screens.storyline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetTaskDetailUseCase
import com.bevietnam.core.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái giao diện (UI State) cho màn hình Chi tiết Nhiệm vụ (TaskDetail).
 */
sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Success(val task: Task) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

/**
 * ViewModel quản lý logic và trạng thái màn hình Chi tiết Nhiệm vụ (TaskDetailScreen).
 *
 * Nhận `taskId` từ navigation argument qua [SavedStateHandle] và tải thông tin chi tiết
 * nhiệm vụ tương ứng thông qua [GetTaskDetailUseCase].
 *
 * @property getTaskDetailUseCase UseCase lấy chi tiết nhiệm vụ theo ID.
 * @property savedStateHandle Handle chứa navigation argument (taskId).
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskDetailUseCase: GetTaskDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTaskDetail()
    }

    private fun loadTaskDetail() {
        if (taskId.isBlank()) {
            _uiState.value = TaskDetailUiState.Error("Không tìm thấy mã nhiệm vụ")
            return
        }

        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            try {
                getTaskDetailUseCase(taskId).collect { task ->
                    if (task != null) {
                        _uiState.value = TaskDetailUiState.Success(task)
                    } else {
                        _uiState.value = TaskDetailUiState.Error("Không tìm thấy nhiệm vụ")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }
}
