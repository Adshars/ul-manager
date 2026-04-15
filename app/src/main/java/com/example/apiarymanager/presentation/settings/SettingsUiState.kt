package com.example.apiarymanager.presentation.settings

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false
)

sealed interface SettingsEvent {
    data object NavigateToLogin : SettingsEvent
    data class ShowMessage(val message: String) : SettingsEvent
}
