package com.example.apiarymanager.presentation.login

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
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            delay(1_000L) // mockowane opóźnienie sieciowe
            _uiState.update { it.copy(isLoading = false) }
            _events.send(LoginEvent.NavigateToDashboard)
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch { _events.send(LoginEvent.NavigateToRegister) }
    }

    fun onForgotPasswordClick() {
        viewModelScope.launch { _events.send(LoginEvent.NavigateToForgotPassword) }
    }

    private fun validate(): Boolean {
        var isValid = true
        val state = _uiState.value

        val emailError = when {
            state.email.isBlank()              -> "Podaj adres e-mail"
            !state.email.contains('@')         -> "Nieprawidłowy adres e-mail"
            else                               -> null
        }
        val passwordError = when {
            state.password.isBlank()           -> "Podaj hasło"
            state.password.length < 6          -> "Hasło musi mieć min. 6 znaków"
            else                               -> null
        }

        if (emailError != null || passwordError != null) isValid = false

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        return isValid
    }
}
