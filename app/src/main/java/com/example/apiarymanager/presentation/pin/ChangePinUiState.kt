package com.example.apiarymanager.presentation.pin

data class ChangePinUiState(
    val step: ChangePinStep = ChangePinStep.VERIFY_CURRENT,
    val pin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

enum class ChangePinStep { VERIFY_CURRENT, ENTER_NEW, CONFIRM_NEW }

sealed interface ChangePinEvent {
    data object NavigateBack : ChangePinEvent
    data object TriggerBiometric : ChangePinEvent
    data class  ShowMessage(val message: String) : ChangePinEvent
}
