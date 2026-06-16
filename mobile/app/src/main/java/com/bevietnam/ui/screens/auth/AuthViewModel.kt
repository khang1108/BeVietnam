package com.bevietnam.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.model.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.bevietnam.core.domain.usecase.LoginUseCase
import com.bevietnam.core.domain.usecase.RegisterUseCase
import com.bevietnam.core.domain.session.SessionManager

/**
 * Các thẻ tab phân loại trên màn hình xác thực tài khoản.
 */
enum class AuthTab { 
    /** Tab Đăng nhập */
    LOGIN, 
    /** Tab Đăng ký tài khoản mới */
    REGISTER 
}

/**
 * Trạng thái giao diện (UI State) cho màn hình Xác thực tài khoản (Auth).
 *
 * @property selectedTab Thẻ tab đang được chọn ([AuthTab]).
 * @property email Địa chỉ email dùng để đăng nhập.
 * @property password Mật khẩu dùng để đăng nhập.
 * @property name Họ và tên đầy đủ khi đăng ký tài khoản mới.
 * @property gender Giới tính lựa chọn khi đăng ký ([Gender]).
 * @property dateOfBirth Ngày sinh nhật kiểu LocalDate khi đăng ký.
 * @property dateOfBirthDisplay Chuỗi định dạng hiển thị ngày sinh nhật (dd/MM/yyyy).
 * @property registerEmail Địa chỉ email sử dụng để đăng ký.
 * @property registerPassword Mật khẩu sử dụng để đăng ký.
 * @property isLoading Trạng thái hệ thống đang gọi API gửi yêu cầu xác thực.
 * @property errorMessage Thông điệp lỗi chi tiết khi xác thực thất bại.
 */
data class AuthUiState(
    val selectedTab: AuthTab = AuthTab.LOGIN,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val gender: Gender? = null,
    val dateOfBirth: LocalDate? = null,
    val dateOfBirthDisplay: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Các sự kiện giao diện diễn ra một lần (One-off UI Events) của màn hình Xác thực.
 */
sealed interface AuthUiEvent {
    /** 
     * Sự kiện điều hướng người dùng sang màn hình Hồ sơ sau đăng nhập thành công.
     * 
     * @property userId Định danh duy nhất của người dùng vừa đăng nhập.
     */
    data class NavigateToProfile(val userId: String) : AuthUiEvent
    
    /** 
     * Sự kiện hiển thị thông báo nhanh (Snackbar) lên màn hình.
     * 
     * @property message Nội dung thông điệp cần hiển thị.
     */
    data class ShowSnackbar(val message: String) : AuthUiEvent
}

/**
 * ViewModel chịu trách nhiệm quản lý logic và trạng thái màn hình Xác thực tài khoản (Auth Screen).
 *
 * Lớp này sử dụng quy chuẩn Unidirectional Data Flow (UDF), xử lý các sự kiện thay đổi
 * trường thông tin, gọi UseCase đăng nhập/đăng ký, và phát ra các sự kiện điều hướng một lần.
 *
 * @property loginUseCase UseCase xử lý nghiệp vụ đăng nhập tài khoản ([LoginUseCase]).
 * @property registerUseCase UseCase xử lý nghiệp vụ đăng ký tài khoản mới ([RegisterUseCase]).
 * @property sessionManager Quản lý phiên đăng nhập và thông tin tài khoản người dùng hiện tại ([SessionManager]).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    /**
     * Thay đổi tab lựa chọn giữa Đăng nhập và Đăng ký.
     *
     * @param tab Tab được lựa chọn ([AuthTab]).
     */
    fun selectTab(tab: AuthTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    /**
     * Nhận sự kiện thay đổi email đăng nhập từ UI.
     *
     * @param email Địa chỉ email mới.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Nhận sự kiện thay đổi mật khẩu đăng nhập từ UI.
     *
     * @param password Mật khẩu mới.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Nhận sự kiện thay đổi họ tên đăng ký từ UI.
     *
     * @param name Họ tên đầy đủ mới.
     */
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    /**
     * Nhận sự kiện thay đổi giới tính đăng ký từ UI.
     *
     * @param gender Giới tính lựa chọn ([Gender]).
     */
    fun onGenderChange(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    /**
     * Nhận sự kiện chọn ngày sinh từ hộp thoại UI.
     * Định dạng ngày sinh về dạng chuỗi "dd/MM/yyyy" thân thiện để hiển thị và lưu trữ.
     *
     * @param date Đối tượng ngày sinh [LocalDate].
     */
    fun onDateOfBirthChange(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        _uiState.update { 
            it.copy(
                dateOfBirth = date,
                dateOfBirthDisplay = date.format(formatter)
            ) 
        }
    }

    /**
     * Nhận sự kiện thay đổi email đăng ký từ UI.
     *
     * @param email Email đăng ký mới.
     */
    fun onRegisterEmailChange(email: String) {
        _uiState.update { it.copy(registerEmail = email) }
    }

    /**
     * Nhận sự kiện thay đổi mật khẩu đăng ký từ UI.
     *
     * @param password Mật khẩu đăng ký mới.
     */
    fun onRegisterPasswordChange(password: String) {
        _uiState.update { it.copy(registerPassword = password) }
    }

    /**
     * Thực hiện nghiệp vụ đăng nhập tài khoản.
     * Gọi UseCase đăng nhập và xử lý kết quả:
     * - Thành công: Lưu phiên đăng nhập và kích hoạt sự kiện điều hướng sang trang hồ sơ.
     * - Thất bại: Cập nhật thông điệp lỗi lên giao diện.
     */
    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password
            ).collect { result ->
                result.onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false) }
                    sessionManager.login(user)
                    _uiEvent.emit(AuthUiEvent.NavigateToProfile(user.id))
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = exception.message) 
                    }
                }
            }
        }
    }

    /**
     * Thực hiện nghiệp vụ đăng ký tài khoản người dùng mới.
     * Gọi UseCase đăng ký tài khoản và xử lý kết quả:
     * - Thành công: Hiển thị Snackbar thông báo đăng ký thành công và tự động chuyển sang tab Đăng nhập.
     * - Thất bại: Cập nhật thông điệp lỗi lên giao diện để người dùng sửa đổi.
     */
    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            registerUseCase(
                name = state.name,
                gender = state.gender,
                dateOfBirth = state.dateOfBirthDisplay,
                email = state.registerEmail,
                password = state.registerPassword
            ).collect { result ->
                result.onSuccess { _ ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar("Đăng ký thành công! Vui lòng đăng nhập."))
                    selectTab(AuthTab.LOGIN)
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = exception.message) 
                    }
                }
            }
        }
    }

    /**
     * Nhận sự kiện người dùng click "Quên mật khẩu" từ UI.
     * Phát ra sự kiện Snackbar hiển thị thông báo tạm thời do tính năng đang được phát triển.
     */
    fun onForgotPassword() {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.ShowSnackbar("Tính năng đang phát triển"))
        }
    }
}
