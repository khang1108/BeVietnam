package com.bevietnam.ui.screens.storyline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetTasksUseCase
import com.bevietnam.core.domain.usecase.CompleteTaskUseCase
import com.bevietnam.core.domain.usecase.GetNextTaskUseCase
import com.bevietnam.core.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái giao diện (UI State) cho màn hình Hành trình văn hóa (Storyline).
 */
sealed class StorylineUiState {
    /** Trạng thái đang tải dữ liệu */
    object Loading : StorylineUiState()
    
    /** 
     * Trạng thái tải dữ liệu thành công.
     * 
     * @property tasks Danh sách tất cả thử thách/nhiệm vụ du lịch.
     * @property nextTask Nhiệm vụ tiếp theo người dùng cần hoàn thành.
     */
    data class Success(
        val tasks: List<Task>,
        val nextTask: Task?
    ) : StorylineUiState()
    
    /** Trạng thái danh sách nhiệm vụ trống */
    object Empty : StorylineUiState()
    
    /** 
     * Trạng thái gặp lỗi khi tải dữ liệu.
     * 
     * @property message Thông tin chi tiết về lỗi xảy ra.
     */
    data class Error(val message: String) : StorylineUiState()
}

/**
 * ViewModel chịu trách nhiệm quản lý logic và trạng thái màn hình Hành trình văn hóa (Storyline).
 *
 * Lớp này tuân thủ Clean Architecture bằng cách chỉ giao tiếp với Domain qua các UseCases,
 * không gọi trực tiếp xuống Repository ở tầng Data.
 *
 * @property getTasksUseCase UseCase lấy danh sách tất cả nhiệm vụ ([GetTasksUseCase]).
 * @property completeTaskUseCase UseCase đánh dấu hoàn thành nhiệm vụ ([CompleteTaskUseCase]).
 * @property getNextTaskUseCase UseCase lấy nhiệm vụ tiếp theo cần hoàn thành ([GetNextTaskUseCase]).
 */
@HiltViewModel
class StorylineViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val getNextTaskUseCase: GetNextTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorylineUiState>(StorylineUiState.Loading)
    val uiState: StateFlow<StorylineUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    /**
     * Tải danh sách nhiệm vụ khám phá văn hóa từ UseCase
     * và cập nhật trạng thái UI tương ứng.
     */
    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = StorylineUiState.Loading
            try {
                getTasksUseCase().collect { tasks ->
                    if (tasks.isEmpty()) {
                        _uiState.value = StorylineUiState.Empty
                    } else {
                        _uiState.value = StorylineUiState.Success(
                            tasks = tasks,
                            nextTask = getNextTaskUseCase().first()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = StorylineUiState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }

    /**
     * Đánh dấu hoàn thành một nhiệm vụ cụ thể dựa trên ID.
     *
     * @param taskId Định danh duy nhất của nhiệm vụ đã hoàn thành.
     */
    fun onTaskCompleted(taskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(taskId)
            loadTasks()
        }
    }
}
