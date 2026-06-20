package com.bevietnam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.session.SessionManager
import com.bevietnam.core.domain.usecase.CheckHealthUseCase
import com.bevietnam.core.model.HealthStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bevietnam.core.model.User
import com.bevietnam.core.data.local.TokenStorage
import com.bevietnam.core.domain.usecase.GetUserUseCase
@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val checkHealthUseCase: CheckHealthUseCase,
    private val tokenStorage: TokenStorage,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = sessionManager.currentUser

    private val _backendStatus = MutableStateFlow<Result<HealthStatus>?>(null)
    val backendStatus: StateFlow<Result<HealthStatus>?> = _backendStatus.asStateFlow()

    init {
        checkBackendHealth()
        checkAutoLogin()
    }

    private fun checkBackendHealth() {
        viewModelScope.launch {
            checkHealthUseCase().collect { result ->
                _backendStatus.value = result
            }
        }
    }

    private fun checkAutoLogin() {
        val token = tokenStorage.getToken()
        if (!token.isNullOrBlank()) {
            viewModelScope.launch {
                getUserUseCase("me").collect { result ->
                    result.onSuccess { user ->
                        sessionManager.login(user)
                    }
                }
            }
        }
    }
}
