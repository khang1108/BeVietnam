package com.bevietnam.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetUserUseCase
import com.bevietnam.core.domain.usecase.UpdateUserUseCase
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import com.bevietnam.core.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    // Fields for editing
    val editName: String = "",
    val editBio: String = "",
    val editGender: Gender? = null,
    val editDateOfBirth: String = "",
    val editLocation: String = ""
)

sealed class ProfileUiEvent {
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
    data object NavigateToLogin : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadUserProfile()
    }

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

    fun onNameChange(value: String) {
        _uiState.update { it.copy(editName = value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(editBio = value) }
    }

    fun onGenderChange(value: Gender) {
        _uiState.update { it.copy(editGender = value) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(editDateOfBirth = value) }
    }

    fun onLocationChange(value: String) {
        _uiState.update { it.copy(editLocation = value) }
    }

    fun toggleEditMode() {
        _uiState.update { state ->
            if (!state.isEditMode) {
                // Khi bắt đầu edit, copy data hiện tại vào form
                state.copy(
                    isEditMode = true,
                    editName = state.user?.name.orEmpty(),
                    editBio = state.user?.bio.orEmpty(),
                    editGender = state.user?.gender,
                    editDateOfBirth = state.user?.dateOfBirth.orEmpty(),
                    editLocation = state.user?.location.orEmpty()
                )
            } else {
                state.copy(isEditMode = false)
            }
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val currentUser = currentState.user ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val updatedUser = currentUser.copy(
                name = currentState.editName,
                bio = currentState.editBio,
                gender = currentState.editGender,
                dateOfBirth = currentState.editDateOfBirth,
                location = currentState.editLocation
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

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            _uiEvent.emit(ProfileUiEvent.NavigateToLogin)
        }
    }
}
