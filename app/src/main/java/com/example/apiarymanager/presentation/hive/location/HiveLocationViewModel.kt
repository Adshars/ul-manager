package com.example.apiarymanager.presentation.hive.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.presentation.navigation.HiveLocationRoute
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
class HiveLocationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository
) : ViewModel() {

    private val hiveId: Long = savedStateHandle.toRoute<HiveLocationRoute>().hiveId

    private val _events = Channel<HiveLocationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _uiState = MutableStateFlow(HiveLocationUiState())
    val uiState: StateFlow<HiveLocationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hiveRepository.getHiveById(hiveId).collect { hive ->
                if (hive != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hiveName  = hive.name,
                            latitude  = hive.latitude,
                            longitude = hive.longitude
                        )
                    }
                }
            }
        }
    }

    fun onLocationSelected(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lon) }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val hive = hiveRepository.getHiveById(hiveId).first()
            if (hive == null) {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(HiveLocationEvent.ShowMessage("Nie znaleziono ula"))
                return@launch
            }
            hiveRepository.updateHive(
                hive.copy(
                    latitude  = _uiState.value.latitude,
                    longitude = _uiState.value.longitude
                )
            )
            _uiState.update { it.copy(isSaving = false) }
            _events.send(HiveLocationEvent.NavigateBack)
        }
    }
}
