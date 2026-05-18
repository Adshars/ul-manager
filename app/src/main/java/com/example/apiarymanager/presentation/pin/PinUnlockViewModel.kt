package com.example.apiarymanager.presentation.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.core.auth.MsalAuthManager
import com.example.apiarymanager.core.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class PinUnlockViewModel @Inject constructor(
    private val pinManager: PinManager,
    private val msalAuthManager: MsalAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PinUnlockUiState(
            isBiometricEnabled = pinManager.isBiometricEnabled
        )
    )
    val uiState: StateFlow<PinUnlockUiState> = _uiState.asStateFlow()

    private val _events = Channel<PinUnlockEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onBiometricAvailabilityChanged(available: Boolean) {
        _uiState.update { it.copy(isBiometricAvailable = available) }
        if (available && pinManager.isBiometricEnabled) {
            viewModelScope.launch { _events.send(PinUnlockEvent.TriggerBiometric) }
        }
    }

    fun onDigitEntered(digit: String) {
        val current = _uiState.value.pin
        if (current.length >= 4) return
        val newPin = current + digit
        _uiState.update { it.copy(pin = newPin, error = null) }
        if (newPin.length == 4) verifyPin(newPin)
    }

    fun onBackspace() {
        _uiState.update { it.copy(pin = it.pin.dropLast(1), error = null) }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch { _events.send(PinUnlockEvent.NavigateToDashboard) }
    }

    fun onBiometricFailed() {
        val attempts = _uiState.value.biometricFailedAttempts + 1
        _uiState.update {
            it.copy(
                biometricFailedAttempts = attempts,
                error = if (attempts >= PinUnlockUiState.MAX_BIOMETRIC_ATTEMPTS)
                    "Zbyt wiele nieudanych prób. Wprowadź PIN."
                else null
            )
        }
    }

    fun onBiometricCancelled() {
        _uiState.update { it.copy(biometricFailedAttempts = PinUnlockUiState.MAX_BIOMETRIC_ATTEMPTS) }
    }

    fun onForgotPinClick() {
        _uiState.update { it.copy(showForgotPinDialog = true) }
    }

    fun onDismissForgotPinDialog() {
        _uiState.update { it.copy(showForgotPinDialog = false) }
    }

    fun onForgotPinConfirmed() {
        if (_uiState.value.isLoggingOut) return
        viewModelScope.launch {
            _uiState.update { it.copy(showForgotPinDialog = false, isLoggingOut = true) }
            withTimeoutOrNull(5_000) { msalAuthManager.signOut() }
            pinManager.clearPin()
            pinManager.isOnboardingDone = false
            _events.send(PinUnlockEvent.NavigateToLogin)
        }
    }

    private fun verifyPin(pin: String) {
        if (pinManager.verifyPin(pin)) {
            viewModelScope.launch { _events.send(PinUnlockEvent.NavigateToDashboard) }
        } else {
            _uiState.update { it.copy(pin = "", error = "Nieprawidłowy PIN") }
        }
    }
}
