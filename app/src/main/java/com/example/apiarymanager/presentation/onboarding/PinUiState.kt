package com.example.apiarymanager.presentation.onboarding

data class PinUiState(
    val step: PinStep = PinStep.ENTER,
    val pin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

enum class PinStep { ENTER, CONFIRM }

sealed interface PinEvent {
    data object NavigateToDashboard : PinEvent
    data class ShowMessage(val message: String) : PinEvent
}
