package com.example.apiarymanager.presentation.pin

data class PinUnlockUiState(
    val pin: String = "",
    val error: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isLoggingOut: Boolean = false,
    val showForgotPinDialog: Boolean = false,
    val biometricFailedAttempts: Int = 0
) {
    val isInBiometricMode: Boolean
        get() = isBiometricEnabled && isBiometricAvailable && biometricFailedAttempts < MAX_BIOMETRIC_ATTEMPTS

    companion object {
        const val MAX_BIOMETRIC_ATTEMPTS = 3
    }
}

sealed interface PinUnlockEvent {
    data object NavigateToDashboard : PinUnlockEvent
    data object NavigateToLogin    : PinUnlockEvent
    data object TriggerBiometric   : PinUnlockEvent
    data class  ShowMessage(val message: String) : PinUnlockEvent
}
