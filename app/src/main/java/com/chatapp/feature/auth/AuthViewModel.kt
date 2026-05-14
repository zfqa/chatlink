package com.chatapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isCheckingSession: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLocalSession()
    }

    private fun checkLocalSession() {
        viewModelScope.launch {
            val hasToken = authRepo.isLoggedIn()
            _uiState.value = if (hasToken) {
                AuthUiState(isLoggedIn = true, isCheckingSession = false)
            } else {
                AuthUiState(isCheckingSession = false)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                authRepo.login(email, password)
                _uiState.value = AuthUiState(isLoggedIn = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String, nickname: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                authRepo.register(email, password, nickname)
                _uiState.value = AuthUiState(isLoggedIn = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message ?: "Registration failed")
            }
        }
    }
}
