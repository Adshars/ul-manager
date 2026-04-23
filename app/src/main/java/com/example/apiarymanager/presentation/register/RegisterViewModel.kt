package com.example.apiarymanager.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onRegisterClick() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1_000L) // mockowane opóźnienie sieciowe
            _uiState.update { it.copy(isLoading = false) }
            _events.send(RegisterEvent.NavigateToDashboard)
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(RegisterEvent.NavigateBack) }
    }

    private fun validate(): Boolean {
        val state = _uiState.value

        val fullNameError    = if (state.fullName.isBlank()) "Podaj nazwę użytkownika" else null
        val emailError       = when {
            state.email.isBlank()          -> "Podaj adres e-mail"
            !state.email.contains('@')     -> "Nieprawidłowy adres e-mail"
            else                           -> null
        }
        val passwordError    = when {
            state.password.isBlank()       -> "Podaj hasło"
            state.password.length < 6      -> "Hasło musi mieć min. 6 znaków"
            else                           -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank()              -> "Potwierdź hasło"
            state.confirmPassword != state.password      -> "Hasła nie są zgodne"
            else                                         -> null
        }

        _uiState.update {
            it.copy(
                fullNameError        = fullNameError,
                emailError           = emailError,
                passwordError        = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }
        return listOf(fullNameError, emailError, passwordError, confirmPasswordError).all { it == null }
    }
}
