package com.example.apiarymanager.presentation.treatment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Treatment
import com.example.apiarymanager.domain.repository.TreatmentRepository
import com.example.apiarymanager.presentation.navigation.TreatmentFormRoute
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
class TreatmentFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TreatmentRepository
) : ViewModel() {

    private val route: TreatmentFormRoute = savedStateHandle.toRoute()
    private val hiveId      = route.hiveId
    private val treatmentId = route.treatmentId

    private val _uiState = MutableStateFlow(TreatmentFormUiState())
    val uiState: StateFlow<TreatmentFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<TreatmentFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { treatmentId?.let { loadTreatment(it) } }

    private fun loadTreatment(id: Long) {
        viewModelScope.launch {
            repository.getTreatmentById(id).collect { t ->
                t?.let { _uiState.update { _ -> TreatmentFormUiState(date = t.date, medicineType = t.medicineType, dosage = t.dosage, applicationMethod = t.applicationMethod, mortalityAfterTreatment = t.mortalityAfterTreatment) } }
            }
        }
    }

    fun onDateChange(v: LocalDate)              { _uiState.update { it.copy(date = v) } }
    fun onMedicineTypeChange(v: String)         { _uiState.update { it.copy(medicineType = v) } }
    fun onDosageChange(v: String)               { _uiState.update { it.copy(dosage = v) } }
    fun onApplicationMethodChange(v: String)    { _uiState.update { it.copy(applicationMethod = v) } }
    fun onMortalityChange(v: String)            { _uiState.update { it.copy(mortalityAfterTreatment = v) } }
    fun onBackClick()                           { viewModelScope.launch { _events.send(TreatmentFormEvent.NavigateBack) } }

    fun onSaveClick() {
        val state = _uiState.value
        val treatment = Treatment(
            id = treatmentId ?: 0, hiveId = hiveId,
            date = state.date, medicineType = state.medicineType,
            dosage = state.dosage, applicationMethod = state.applicationMethod,
            mortalityAfterTreatment = state.mortalityAfterTreatment
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (treatmentId == null) repository.insertTreatment(treatment)
            else                     repository.updateTreatment(treatment)
            _events.send(TreatmentFormEvent.NavigateBack)
        }
    }
}
