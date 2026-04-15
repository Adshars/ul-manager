package com.example.apiarymanager.presentation.harvest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import com.example.apiarymanager.presentation.navigation.HarvestFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HarvestFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HoneyHarvestRepository
) : ViewModel() {

    private val route: HarvestFormRoute = savedStateHandle.toRoute()
    private val hiveId    = route.hiveId
    private val harvestId = route.harvestId

    private val _uiState = MutableStateFlow(HarvestFormUiState())
    val uiState: StateFlow<HarvestFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<HarvestFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { harvestId?.let { loadHarvest(it) } }

    private fun loadHarvest(id: Long) {
        viewModelScope.launch {
            repository.getHarvestById(id).collect { harvest ->
                harvest?.let { h ->
                    _uiState.update {
                        it.copy(date = h.date, honeyType = h.honeyType, weightKg = h.weightKg.toString(), notes = h.notes)
                    }
                }
            }
        }
    }

    fun onDateChange(v: LocalDate)    { _uiState.update { it.copy(date = v) } }
    fun onHoneyTypeChange(v: String)  { _uiState.update { it.copy(honeyType = v) } }
    fun onWeightKgChange(v: String)   { _uiState.update { it.copy(weightKg = v, weightError = null) } }
    fun onNotesChange(v: String)      { _uiState.update { it.copy(notes = v) } }
    fun onBackClick()                 { viewModelScope.launch { _events.send(HarvestFormEvent.NavigateBack) } }

    fun onSaveClick() {
        val state    = _uiState.value
        val weightFloat = state.weightKg.toFloatOrNull()
        if (weightFloat == null || weightFloat <= 0f) {
            _uiState.update { it.copy(weightError = "Podaj prawidłową wagę") }
            return
        }
        val harvest = HoneyHarvest(
            id = harvestId ?: 0, hiveId = hiveId,
            date = state.date, honeyType = state.honeyType,
            weightKg = weightFloat, notes = state.notes
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (harvestId == null) repository.insertHarvest(harvest)
            else                   repository.updateHarvest(harvest)
            _events.send(HarvestFormEvent.NavigateBack)
        }
    }
}
