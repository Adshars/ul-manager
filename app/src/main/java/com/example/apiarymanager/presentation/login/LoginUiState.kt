package com.example.apiarymanager.presentation.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val generalError: String? = null
)

sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
    data object NavigateToRegister : LoginEvent
}
