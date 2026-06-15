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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val checkHealthUseCase: CheckHealthUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = sessionManager.currentUser

    private val _backendStatus = MutableStateFlow<Result<HealthStatus>?>(null)
    val backendStatus: StateFlow<Result<HealthStatus>?> = _backendStatus.asStateFlow()

    init {
        checkBackendHealth()
    }

    private fun checkBackendHealth() {
        viewModelScope.launch {
            checkHealthUseCase().collect { result ->
                _backendStatus.value = result
            }
        }
    }
}
