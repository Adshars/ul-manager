package com.example.apiarymanager.presentation.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.core.auth.MsalAuthManager
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
class LoginViewModel @Inject constructor(
    private val msalAuthManager: MsalAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (msalAuthManager.isSignedIn()) {
                _events.send(LoginEvent.NavigateToDashboard)
            }
        }
    }

    fun onLoginClick(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            msalAuthManager.signIn(activity)
                .onSuccess { _events.send(LoginEvent.NavigateToDashboard) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, generalError = e.message ?: "Błąd logowania")
                    }
                }
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch { _events.send(LoginEvent.NavigateToRegister) }
    }
}
