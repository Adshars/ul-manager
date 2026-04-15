package com.example.apiarymanager.presentation.feeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.repository.FeedingRepository
import com.example.apiarymanager.presentation.navigation.FeedingFormRoute
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
class FeedingFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FeedingRepository
) : ViewModel() {

    private val route: FeedingFormRoute = savedStateHandle.toRoute()
    private val hiveId    = route.hiveId
    private val feedingId = route.feedingId

    private val _uiState = MutableStateFlow(FeedingFormUiState())
    val uiState: StateFlow<FeedingFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<FeedingFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { feedingId?.let { loadFeeding(it) } }

    private fun loadFeeding(id: Long) {
        viewModelScope.launch {
            repository.getFeedingById(id).collect { f ->
                f?.let { _uiState.update { _ -> FeedingFormUiState(date = f.date, foodType = f.foodType, weightKg = f.weightKg.toString(), applicationMethod = f.applicationMethod) } }
            }
        }
    }

    fun onDateChange(v: LocalDate)           { _uiState.update { it.copy(date = v) } }
    fun onFoodTypeChange(v: String)          { _uiState.update { it.copy(foodType = v) } }
    fun onWeightKgChange(v: String)          { _uiState.update { it.copy(weightKg = v, weightError = null) } }
    fun onApplicationMethodChange(v: String) { _uiState.update { it.copy(applicationMethod = v) } }
    fun onBackClick()                        { viewModelScope.launch { _events.send(FeedingFormEvent.NavigateBack) } }

    fun onSaveClick() {
        val state = _uiState.value
        val weightFloat = state.weightKg.toFloatOrNull()
        if (weightFloat == null || weightFloat <= 0f) {
            _uiState.update { it.copy(weightError = "Podaj prawidłową wagę") }
            return
        }
        val feeding = Feeding(
            id = feedingId ?: 0, hiveId = hiveId,
            date = state.date, foodType = state.foodType,
            weightKg = weightFloat, applicationMethod = state.applicationMethod
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (feedingId == null) repository.insertFeeding(feeding)
            else                   repository.updateFeeding(feeding)
            _events.send(FeedingFormEvent.NavigateBack)
        }
    }
}
