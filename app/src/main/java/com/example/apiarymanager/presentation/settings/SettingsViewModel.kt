package com.example.apiarymanager.presentation.settings

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
class SettingsViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isBiometricEnabled = pinManager.isBiometricEnabled))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onBiometricAvailabilityChanged(available: Boolean) {
        _uiState.update { it.copy(isBiometricAvailable = available) }
    }

    fun onBiometricToggle(enabled: Boolean) {
        pinManager.isBiometricEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun onResetPassword() {
        viewModelScope.launch {
            _events.send(SettingsEvent.ShowMessage("Link do resetowania hasła został wysłany na Twój adres e-mail."))
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            // In a real app: clear auth tokens, session data
            pinManager.isOnboardingDone = false
            _events.send(SettingsEvent.NavigateToLogin)
        }
    }
}
