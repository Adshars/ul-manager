package com.example.apiarymanager.presentation.register

data class RegisterUiState(
    val isLoading: Boolean = false,
    val generalError: String? = null
)

sealed interface RegisterEvent {
    data object NavigateToDashboard : RegisterEvent
    data object NavigateBack : RegisterEvent
}
