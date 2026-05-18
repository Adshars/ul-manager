package com.example.apiarymanager.presentation.settings

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isDarkMode: Boolean = false,
    val isLoggingOut: Boolean = false
)

sealed interface SettingsEvent {
    data object NavigateToLogin    : SettingsEvent
    data object NavigateToChangePin : SettingsEvent
    data class  ShowMessage(val message: String) : SettingsEvent
}
