package com.example.apiarymanager.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.data.dto.ForgotPasswordRequest
import com.example.apiarymanager.data.remote.api.AuthApi
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
class ForgotPasswordViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(ForgotPasswordEvent.NavigateBack) }
    }

    fun onSendClick() {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Wprowadź poprawny adres e-mail") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = authApi.forgotPassword(ForgotPasswordRequest(email))
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                _events.send(ForgotPasswordEvent.OpenUrl(response.selfServicePasswordResetUrl))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(
                    ForgotPasswordEvent.ShowMessage(
                        "Jeśli konto istnieje, wysłaliśmy link do resetowania hasła na $email"
                    )
                )
            }
        }
    }
}
