package com.example.apiarymanager.presentation.register

data class RegisterUiState(
    val isLoading: Boolean = false,
    val generalError: String? = null
)

sealed interface RegisterEvent {
    data object NavigateToOnboardingPin : RegisterEvent
    data object NavigateBack            : RegisterEvent
}
