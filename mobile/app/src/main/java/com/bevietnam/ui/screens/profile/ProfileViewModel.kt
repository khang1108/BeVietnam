package com.bevietnam.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetUserUseCase
import com.bevietnam.core.domain.usecase.UpdateUserUseCase
import com.bevietnam.core.domain.usecase.GetQuestChainUseCase
import com.bevietnam.core.model.User
import com.bevietnam.core.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái giao diện (UI State) cho màn hình Hồ sơ cá nhân (Profile).
 *
 * @property isLoading Trạng thái đang tải dữ liệu thông tin người dùng.
 * @property user Đối tượng chứa thông tin đầy đủ của người dùng ([User]) hiện tại.
 * @property errorMessage Thông điệp lỗi chi tiết khi không tải được dữ liệu.
 * @property isEditMode Trạng thái màn hình đang ở chế độ chỉnh sửa thông tin hay chỉ đọc.
 * @property isSaving Trạng thái đang thực hiện lưu thông tin chỉnh sửa lên server.
 * @property editName Trường chỉnh sửa Họ và tên.
 * @property editBio Trường chỉnh sửa thông tin mô tả giới thiệu bản thân.
 * @property editGender Trường chỉnh sửa Giới tính.
 * @property editDateOfBirth Trường chỉnh sửa Ngày sinh nhật.
 * @property editLocation Trường chỉnh sửa Địa điểm sinh sống.
 */
data class JourneyImage(val url: String, val note: String?)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val editName: String = "",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val journeyImages: List<JourneyImage> = emptyList()
)

/**
 * Các sự kiện giao diện diễn ra một lần (One-off UI Events) của màn hình Hồ sơ cá nhân.
 */
sealed class ProfileUiEvent {
    /** 
     * Sự kiện hiển thị thông báo nhanh (Snackbar) lên màn hình.
     * 
     * @property message Nội dung thông báo cần hiển thị.
     */
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
    
    /** Sự kiện điều hướng người dùng quay trở lại màn hình Đăng nhập (khi Đăng xuất) */
    data object NavigateToLogin : ProfileUiEvent()
}

/**
 * ViewModel chịu trách nhiệm quản lý logic nghiệp vụ và trạng thái dữ liệu cho màn hình Hồ sơ cá nhân (Profile Screen).
 *
 * Lớp này sử dụng quy chuẩn Unidirectional Data Flow (UDF), điều phối luồng chỉnh sửa thông tin cá nhân,
 * lưu trữ cập nhật hồ sơ, và điều hướng đăng xuất tài khoản một cách an toàn.
 *
 * @property getUserUseCase UseCase lấy thông tin tài khoản người dùng chi tiết ([GetUserUseCase]).
 * @property updateUserUseCase UseCase thực hiện cập nhật thông tin tài khoản ([UpdateUserUseCase]).
 * @property sessionManager Quản lý phiên tài khoản người dùng hiện tại đang đăng nhập ([SessionManager]).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getQuestChainUseCase: GetQuestChainUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadUserProfile()
        loadJourneyStats()
    }

    /**
     * Tải thông tin hồ sơ của người dùng hiện tại đang đăng nhập từ Domain layer.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = sessionManager.currentUser.value?.id ?: return@launch
            getUserUseCase(userId).collect { result ->
                result.onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            }
        }
    }

    /**
     * Tải thông tin thống kê tiến độ khám phá (Gamification) và ảnh Check-in
     */
    private fun loadJourneyStats() {
        viewModelScope.launch {
            getQuestChainUseCase()
                .catch { /* ignore error on stats */ }
                .collect { questChain ->
                    val total = questChain.totalTasks
                    val completed = questChain.tasks.count { it.isCompleted }
                    val images = questChain.tasks
                        .filter { it.isCompleted && !it.captureImageUrl.isNullOrEmpty() }
                        .map { JourneyImage(url = it.captureImageUrl!!, note = it.captureNote) }
                    
                    _uiState.update { 
                        it.copy(
                            totalTasks = total,
                            completedTasks = completed,
                            journeyImages = images
                        )
                    }
                }
        }
    }

    /**
     * Nhận sự kiện thay đổi họ tên chỉnh sửa từ UI.
     *
     * @param value Tên mới.
     */
    fun onNameChange(value: String) {
        _uiState.update { it.copy(editName = value) }
    }



    /**
     * Chuyển đổi trạng thái giữa Chế độ chỉnh sửa (Edit Mode) và Chế độ chỉ đọc (View Mode).
     *
     * Khi chuyển sang Chế độ chỉnh sửa, sao chép toàn bộ thông tin hiện tại của [User]
     * vào các trường chỉnh sửa tạm thời trên form để hiển thị lên UI.
     */
    fun toggleEditMode() {
        _uiState.update { state ->
            if (!state.isEditMode) {
                state.copy(
                    isEditMode = true,
                    editName = state.user?.name.orEmpty()
                )
            } else {
                state.copy(isEditMode = false)
            }
        }
    }

    /**
     * Thực hiện nghiệp vụ lưu thông tin hồ sơ đã chỉnh sửa.
     * Sao chép các thông tin thay đổi vào đối tượng [User] hiện tại, gọi UseCase cập nhật,
     * và phát ra Snackbar thông báo kết quả.
     */
    fun saveProfile() {
        val currentState = _uiState.value
        val currentUser = currentState.user ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val updatedUser = currentUser.copy(
                name = currentState.editName
            )

            updateUserUseCase(updatedUser).collect { result ->
                result.onSuccess {
                    _uiState.update { 
                        it.copy(user = updatedUser, isSaving = false, isEditMode = false) 
                    }
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Cập nhật thành công"))
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar(e.message ?: "Có lỗi xảy ra"))
                }
            }
        }
    }

    /**
     * Thực hiện nghiệp vụ đăng xuất tài khoản.
     * Xóa sạch phiên đăng nhập lưu trữ trong [SessionManager] và phát ra sự kiện
     * điều hướng người dùng quay về màn hình Đăng nhập.
     */
    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            _uiEvent.emit(ProfileUiEvent.NavigateToLogin)
        }
    }
}
