package com.bevietnam.core.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    private val _currentUser = MutableStateFlow<com.bevietnam.core.model.User?>(null)
    val currentUser: StateFlow<com.bevietnam.core.model.User?> = _currentUser.asStateFlow()

    // Giữ lại isLoggedIn cho tiện nếu có nơi khác dùng
    val isLoggedIn: StateFlow<Boolean> = _currentUser.map { it != null }
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun login(user: com.bevietnam.core.model.User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }
}
