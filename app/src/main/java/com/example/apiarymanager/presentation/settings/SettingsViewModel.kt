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

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isBiometricEnabled = pinManager.isBiometricEnabled,
            isDarkMode         = pinManager.isDarkMode,
            isPinSet           = pinManager.isPinSet
        )
    )
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

    fun onDarkModeToggle(enabled: Boolean) {
        pinManager.isDarkMode = enabled
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun onChangePasswordClick() {
        _uiState.update {
            it.copy(
                showChangePasswordDialog = true,
                oldPassword   = "",
                newPassword   = "",
                confirmPassword = "",
                passwordError = null
            )
        }
    }

    fun onDismissChangePassword() {
        _uiState.update { it.copy(showChangePasswordDialog = false) }
    }

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value, passwordError = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, passwordError = null) }
    }

    fun onConfirmChangePassword() {
        val state = _uiState.value
        if (state.isPinSet && !pinManager.verifyPin(state.oldPassword)) {
            _uiState.update { it.copy(passwordError = "Stare hasło jest nieprawidłowe") }
            return
        }
        if (state.newPassword.isBlank()) {
            _uiState.update { it.copy(passwordError = "Nowe hasło nie może być puste") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(passwordError = "Hasła nie są zgodne") }
            return
        }
        pinManager.setPin(state.newPassword)
        _uiState.update { it.copy(showChangePasswordDialog = false, isPinSet = true, oldPassword = "", newPassword = "", confirmPassword = "") }
        viewModelScope.launch {
            _events.send(SettingsEvent.ShowMessage("Hasło zostało zmienione"))
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            pinManager.isOnboardingDone = false
            _events.send(SettingsEvent.NavigateToLogin)
        }
    }
}
