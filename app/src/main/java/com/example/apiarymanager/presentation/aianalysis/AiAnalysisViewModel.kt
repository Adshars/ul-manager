package com.example.apiarymanager.presentation.aianalysis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.AnalysisRequest
import com.example.apiarymanager.domain.model.AnalysisType
import com.example.apiarymanager.domain.model.HivePhoto
import com.example.apiarymanager.domain.repository.HivePhotoRepository
import com.example.apiarymanager.domain.repository.PhotoAnalysisRepository
import com.example.apiarymanager.presentation.navigation.AiAnalysisRoute
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
class AiAnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hivePhotoRepository: HivePhotoRepository,
    private val photoAnalysisRepository: PhotoAnalysisRepository
) : ViewModel() {

    private val hiveId: Long = savedStateHandle.toRoute<AiAnalysisRoute>().hiveId

    private val _uiState = MutableStateFlow(AiAnalysisUiState())
    val uiState: StateFlow<AiAnalysisUiState> = _uiState.asStateFlow()

    private val _events = Channel<AiAnalysisEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val photos = hivePhotoRepository.getPhotosByHive(hiveId).first()
            _uiState.update { it.copy(photos = photos) }
        }
    }

    fun onPhotoSelected(photo: HivePhoto) {
        _uiState.update { it.copy(selectedPhoto = photo, step = AiAnalysisStep.FORM) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onAnalysisTypeChange(type: AnalysisType) {
        _uiState.update { it.copy(analysisType = type) }
    }

    fun onBackToPhotoSelect() {
        _uiState.update { it.copy(step = AiAnalysisStep.SELECT_PHOTO, selectedPhoto = null) }
    }

    fun onRunAnalysis() {
        val photo = _uiState.value.selectedPhoto ?: return
        _uiState.update { it.copy(step = AiAnalysisStep.ANALYZING) }
        viewModelScope.launch {
            val result = photoAnalysisRepository.analyzePhoto(
                AnalysisRequest(
                    photoPath = photo.localPath,
                    note      = _uiState.value.note,
                    type      = _uiState.value.analysisType
                )
            )
            _uiState.update {
                it.copy(
                    step          = AiAnalysisStep.RESULT,
                    resultMessage = result.message,
                    isSuccess     = result.isSuccess
                )
            }
        }
    }

    fun onFinish()    { viewModelScope.launch { _events.send(AiAnalysisEvent.NavigateBack) } }
    fun onBackClick() { viewModelScope.launch { _events.send(AiAnalysisEvent.NavigateBack) } }
}
