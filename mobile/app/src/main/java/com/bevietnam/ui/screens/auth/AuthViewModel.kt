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
import com.bevietnam.core.session.SessionManager

enum class AuthTab { LOGIN, REGISTER }

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

sealed interface AuthUiEvent {
    data class NavigateToProfile(val userId: String) : AuthUiEvent
    data class ShowSnackbar(val message: String) : AuthUiEvent
}

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

    fun selectTab(tab: AuthTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onGenderChange(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onDateOfBirthChange(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        _uiState.update { 
            it.copy(
                dateOfBirth = date,
                dateOfBirthDisplay = date.format(formatter)
            ) 
        }
    }

    fun onRegisterEmailChange(email: String) {
        _uiState.update { it.copy(registerEmail = email) }
    }

    fun onRegisterPasswordChange(password: String) {
        _uiState.update { it.copy(registerPassword = password) }
    }

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
                result.onSuccess { user ->
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

    fun onForgotPassword() {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.ShowSnackbar("Tính năng đang phát triển"))
        }
    }
}
