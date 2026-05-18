package com.example.apiarymanager.presentation.pin

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
class ChangePinViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChangePinUiState(isBiometricEnabled = pinManager.isBiometricEnabled)
    )
    val uiState: StateFlow<ChangePinUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChangePinEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onBiometricAvailabilityChanged(available: Boolean) {
        _uiState.update { it.copy(isBiometricAvailable = available) }
        if (available && pinManager.isBiometricEnabled &&
            _uiState.value.step == ChangePinStep.VERIFY_CURRENT) {
            viewModelScope.launch { _events.send(ChangePinEvent.TriggerBiometric) }
        }
    }

    fun onDigitEntered(digit: String) {
        val state = _uiState.value
        when (state.step) {
            ChangePinStep.VERIFY_CURRENT -> {
                if (state.pin.length >= 4) return
                val newPin = state.pin + digit
                _uiState.update { it.copy(pin = newPin, error = null) }
                if (newPin.length == 4) verifyCurrent(newPin)
            }
            ChangePinStep.ENTER_NEW -> {
                if (state.newPin.length >= 4) return
                val newPin = state.newPin + digit
                _uiState.update { it.copy(newPin = newPin, error = null) }
                if (newPin.length == 4) {
                    _uiState.update { it.copy(step = ChangePinStep.CONFIRM_NEW) }
                }
            }
            ChangePinStep.CONFIRM_NEW -> {
                if (state.confirmPin.length >= 4) return
                val newConfirm = state.confirmPin + digit
                _uiState.update { it.copy(confirmPin = newConfirm, error = null) }
                if (newConfirm.length == 4) saveNewPin(state.newPin, newConfirm)
            }
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            when (state.step) {
                ChangePinStep.VERIFY_CURRENT -> state.copy(pin = state.pin.dropLast(1))
                ChangePinStep.ENTER_NEW      -> state.copy(newPin = state.newPin.dropLast(1))
                ChangePinStep.CONFIRM_NEW    -> state.copy(confirmPin = state.confirmPin.dropLast(1))
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.update { it.copy(step = ChangePinStep.ENTER_NEW, pin = "", error = null) }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(ChangePinEvent.NavigateBack) }
    }

    private fun verifyCurrent(pin: String) {
        if (pinManager.verifyPin(pin)) {
            _uiState.update { it.copy(step = ChangePinStep.ENTER_NEW, pin = "", error = null) }
        } else {
            _uiState.update { it.copy(pin = "", error = "Nieprawidłowy PIN") }
        }
    }

    private fun saveNewPin(newPin: String, confirmPin: String) {
        if (newPin != confirmPin) {
            _uiState.update {
                it.copy(
                    step        = ChangePinStep.ENTER_NEW,
                    newPin      = "",
                    confirmPin  = "",
                    error       = "PINy nie są zgodne. Spróbuj ponownie."
                )
            }
            return
        }
        pinManager.setPin(newPin)
        viewModelScope.launch {
            _events.send(ChangePinEvent.ShowMessage("PIN został zmieniony"))
            _events.send(ChangePinEvent.NavigateBack)
        }
    }
}
