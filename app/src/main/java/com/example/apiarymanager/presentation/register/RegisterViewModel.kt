package com.example.apiarymanager.presentation.register

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.core.auth.MsalAuthManager
import com.example.apiarymanager.data.dto.RegisterRequest
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
class RegisterViewModel @Inject constructor(
    private val msalAuthManager: MsalAuthManager,
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onRegisterClick(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            msalAuthManager.signIn(activity)
                .onSuccess {
                    runCatching { authApi.register(RegisterRequest()) }
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.NavigateToDashboard)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, generalError = e.message ?: "Błąd rejestracji")
                    }
                }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(RegisterEvent.NavigateBack) }
    }
}
