package com.example.apiarymanager.presentation.hive.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.HiveStatus
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.presentation.navigation.HiveFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HiveFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository
) : ViewModel() {

    private val route: HiveFormRoute = savedStateHandle.toRoute()
    private val apiaryId = route.apiaryId
    private val hiveId   = route.hiveId

    private val _uiState = MutableStateFlow(
        HiveFormUiState(qrCode = UUID.randomUUID().toString())
    )
    val uiState: StateFlow<HiveFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<HiveFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        hiveId?.let { loadHive(it) }
    }

    private fun loadHive(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            hiveRepository.getHiveById(id).collect { hive ->
                hive?.let { h ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            name         = h.name,
                            number       = h.number.toString(),
                            queenYear    = h.queenYear?.toString() ?: "",
                            frameType    = h.frameType,
                            superboxCount = h.superboxCount.toString(),
                            queenOrigin  = h.queenOrigin,
                            status       = h.status.name,
                            notes        = h.notes,
                            qrCode       = h.qrCode.ifBlank { it.qrCode } // keep generated UUID if DB has none
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(v: String)         { _uiState.update { it.copy(name = v, nameError = null) } }
    fun onNumberChange(v: String)       { _uiState.update { it.copy(number = v, numberError = null) } }
    fun onQueenYearChange(v: String)    { _uiState.update { it.copy(queenYear = v) } }
    fun onFrameTypeChange(v: String)    { _uiState.update { it.copy(frameType = v) } }
    fun onSuperboxCountChange(v: String){ _uiState.update { it.copy(superboxCount = v) } }
    fun onQueenOriginChange(v: String)  { _uiState.update { it.copy(queenOrigin = v) } }
    fun onStatusChange(v: String)       { _uiState.update { it.copy(status = v) } }
    fun onNotesChange(v: String)        { _uiState.update { it.copy(notes = v) } }
    fun onBackClick()                   { viewModelScope.launch { _events.send(HiveFormEvent.NavigateBack) } }

    fun onSaveClick() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nazwa jest wymagana") }
            hasError = true
        }
        val numberInt = state.number.toIntOrNull()
        if (numberInt == null || numberInt <= 0) {
            _uiState.update { it.copy(numberError = "Podaj prawidłowy numer") }
            hasError = true
        }
        if (hasError) return

        val hive = Hive(
            id            = hiveId ?: 0,
            apiaryId      = apiaryId,
            name          = state.name.trim(),
            number        = numberInt!!,
            queenYear     = state.queenYear.toIntOrNull(),
            frameType     = state.frameType,
            superboxCount = state.superboxCount.toIntOrNull() ?: 0,
            queenOrigin   = state.queenOrigin.trim(),
            status        = runCatching { HiveStatus.valueOf(state.status) }.getOrDefault(HiveStatus.ACTIVE),
            notes         = state.notes.trim(),
            qrCode        = state.qrCode.ifBlank { UUID.randomUUID().toString() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (hiveId == null) hiveRepository.insertHive(hive)
            else                hiveRepository.updateHive(hive)
            _uiState.update { it.copy(isSaving = false) }
            _events.send(HiveFormEvent.NavigateBack)
        }
    }
}
