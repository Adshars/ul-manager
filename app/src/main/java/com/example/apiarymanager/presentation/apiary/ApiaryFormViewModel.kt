package com.example.apiarymanager.presentation.apiary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.repository.ApiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiaryFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apiaryRepository: ApiaryRepository
) : ViewModel() {

    private val apiaryId: Long? = savedStateHandle["apiaryId"]

    private val _uiState = MutableStateFlow(ApiaryFormUiState(apiaryId = apiaryId))
    val uiState: StateFlow<ApiaryFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<ApiaryFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (apiaryId != null) loadExisting(apiaryId)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            apiaryRepository.getApiaryById(id).first()
                ?.let { apiary -> _uiState.value = apiary.toFormState() }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onLocationChange(value: String) =
        _uiState.update { it.copy(location = value) }

    fun onNotesChange(value: String) =
        _uiState.update { it.copy(notes = value) }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nazwa pasieki jest wymagana") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                val apiary = state.toApiary()
                if (state.apiaryId == null) apiaryRepository.insertApiary(apiary)
                else apiaryRepository.updateApiary(apiary)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(ApiaryFormEvent.NavigateBack)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false) }
                _events.send(ApiaryFormEvent.ShowMessage("Błąd zapisu: ${e.message}"))
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(ApiaryFormEvent.NavigateBack) }
    }
}
