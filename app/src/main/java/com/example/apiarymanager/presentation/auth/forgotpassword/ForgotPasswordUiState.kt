package com.example.apiarymanager.presentation.auth.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

sealed interface ForgotPasswordEvent {
    data object NavigateBack : ForgotPasswordEvent
    data class ShowMessage(val message: String) : ForgotPasswordEvent
}
