package com.sreeman.mywifi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreeman.mywifi.data.model.LoginCredentials
import com.sreeman.mywifi.data.model.SessionState
import com.sreeman.mywifi.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "admin",
    val password: String = "admin",
    val gatewayIp: String = "192.168.0.1",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isWifiConnected: Boolean = true,
    val detectedSsid: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val sessionState: StateFlow<SessionState> = repository.sessionState

    init {
        loadSavedCredentialsAndDetectGateway()
    }

    fun loadSavedCredentialsAndDetectGateway() {
        viewModelScope.launch {
            val detectedGateway = repository.getDetectedGatewayIp()
            val savedCreds = repository.getSavedCredentials()

            _uiState.update { currentState ->
                currentState.copy(
                    gatewayIp = savedCreds?.gatewayIp ?: detectedGateway,
                    username = savedCreds?.username ?: "admin",
                    password = savedCreds?.password ?: "",
                    rememberMe = savedCreds != null
                )
            }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onGatewayIpChanged(newGatewayIp: String) {
        _uiState.update { it.copy(gatewayIp = newGatewayIp, errorMessage = null) }
    }

    fun onPasswordVisibilityToggled() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRememberMeToggled(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username and password cannot be empty.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val credentials = LoginCredentials(
                username = state.username.trim(),
                password = state.password.trim(),
                gatewayIp = state.gatewayIp.trim()
            )

            val result = repository.authenticate(credentials, saveCredentialsLocally = state.rememberMe)
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }
}
