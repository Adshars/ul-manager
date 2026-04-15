package com.example.apiarymanager.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.core.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private val _events = Channel<PinEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onBiometricAvailabilityChanged(available: Boolean) {
        _uiState.update { it.copy(isBiometricAvailable = available) }
    }

    fun onDigitEntered(digit: String) {
        val state = _uiState.value
        when (state.step) {
            PinStep.ENTER -> {
                if (state.pin.length < 4) {
                    val newPin = state.pin + digit
                    _uiState.update { it.copy(pin = newPin, error = null) }
                    if (newPin.length == 4) {
                        _uiState.update { it.copy(step = PinStep.CONFIRM) }
                    }
                }
            }
            PinStep.CONFIRM -> {
                if (state.confirmPin.length < 4) {
                    val newConfirm = state.confirmPin + digit
                    _uiState.update { it.copy(confirmPin = newConfirm, error = null) }
                    if (newConfirm.length == 4) {
                        validateAndSave(state.pin, newConfirm)
                    }
                }
            }
        }
    }

    fun onBackspace() {
        val state = _uiState.value
        when (state.step) {
            PinStep.ENTER   -> _uiState.update { it.copy(pin = it.pin.dropLast(1)) }
            PinStep.CONFIRM -> _uiState.update { it.copy(confirmPin = it.confirmPin.dropLast(1)) }
        }
    }

    fun onResetStep() {
        _uiState.update { it.copy(step = PinStep.ENTER, pin = "", confirmPin = "", error = null) }
    }

    fun onBiometricToggle(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun onSkip() {
        pinManager.isOnboardingDone = true
        viewModelScope.launch { _events.send(PinEvent.NavigateToDashboard) }
    }

    private fun validateAndSave(pin: String, confirm: String) {
        if (pin != confirm) {
            _uiState.update { it.copy(confirmPin = "", error = "PINy nie są zgodne. Spróbuj ponownie.") }
            viewModelScope.launch { _events.send(PinEvent.ShowMessage("PINy nie są zgodne")) }
            return
        }
        pinManager.setPin(pin)
        pinManager.isBiometricEnabled = _uiState.value.isBiometricEnabled
        pinManager.isOnboardingDone   = true
        viewModelScope.launch { _events.send(PinEvent.NavigateToDashboard) }
    }
}
