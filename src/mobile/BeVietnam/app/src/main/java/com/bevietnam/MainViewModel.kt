package com.bevietnam

import androidx.lifecycle.ViewModel
import com.bevietnam.core.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.bevietnam.core.model.User

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    val currentUser: StateFlow<User?> = sessionManager.currentUser
}
