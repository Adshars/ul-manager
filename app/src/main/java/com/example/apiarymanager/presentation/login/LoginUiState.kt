package com.example.apiarymanager.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
    data object NavigateToRegister : LoginEvent
    data object ShowForgotPasswordInfo : LoginEvent
}
