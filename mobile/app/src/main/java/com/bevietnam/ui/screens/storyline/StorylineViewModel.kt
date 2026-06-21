package com.bevietnam.ui.screens.storyline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetQuestChainUseCase
import com.bevietnam.core.domain.usecase.CompleteTaskUseCase
import com.bevietnam.core.model.QuestChain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
     * @property questChain Chuỗi nhiệm vụ hành trình đầy đủ.
     */
    data class Success(
        val questChain: QuestChain
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
 * @property getQuestChainUseCase UseCase lấy chuỗi nhiệm vụ hành trình ([GetQuestChainUseCase]).
 * @property completeTaskUseCase UseCase đánh dấu hoàn thành nhiệm vụ ([CompleteTaskUseCase]).
 */
@HiltViewModel
class StorylineViewModel @Inject constructor(
    private val getQuestChainUseCase: GetQuestChainUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorylineUiState>(StorylineUiState.Loading)
    val uiState: StateFlow<StorylineUiState> = _uiState.asStateFlow()

    init {
        loadQuestChain()
    }

    /**
     * Tải chuỗi nhiệm vụ hành trình từ UseCase
     * và cập nhật trạng thái UI tương ứng.
     */
    fun loadQuestChain() {
        viewModelScope.launch {
            _uiState.value = StorylineUiState.Loading
            try {
                getQuestChainUseCase().collect { questChain ->
                    if (questChain.tasks.isEmpty()) {
                        _uiState.value = StorylineUiState.Empty
                    } else {
                        _uiState.value = StorylineUiState.Success(questChain = questChain)
                    }
                }
            } catch (e: CancellationException) {
                throw e
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
            loadQuestChain()
        }
    }
}
