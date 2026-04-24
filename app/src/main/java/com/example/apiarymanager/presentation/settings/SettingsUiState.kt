package com.example.apiarymanager.presentation.settings

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isDarkMode: Boolean = false,
    val isPinSet: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null
)

sealed interface SettingsEvent {
    data object NavigateToLogin : SettingsEvent
    data class ShowMessage(val message: String) : SettingsEvent
}
