package com.example.apiarymanager.presentation.inspection

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.model.ColonyStrength
import com.example.apiarymanager.domain.repository.InspectionRepository
import com.example.apiarymanager.domain.usecase.SaveInspectionUseCase
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InspectionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val inspectionRepository: InspectionRepository,
    private val saveInspectionUseCase: SaveInspectionUseCase
) : ViewModel() {

    // Navigation 2.8 places typed-route properties into SavedStateHandle by name.
    private val hiveId: Long = checkNotNull(savedStateHandle["hiveId"])
    private val inspectionId: Long? = savedStateHandle["inspectionId"]

    private val _uiState = MutableStateFlow(
        InspectionFormUiState(hiveId = hiveId, inspectionId = inspectionId)
    )
    val uiState: StateFlow<InspectionFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<InspectionFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (inspectionId != null) loadExistingInspection(inspectionId)
        // Pick up photo path returned from CameraScreen via SavedStateHandle
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("capturedPhotoPath", null).collect { path ->
                if (!path.isNullOrBlank()) {
                    onPhotoAdded(path)
                    savedStateHandle.remove<String>("capturedPhotoPath")
                }
            }
        }
    }

    // ─── Load for edit ────────────────────────────────────────────────────────

    private fun loadExistingInspection(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            inspectionRepository.getInspectionById(id)
                .first()
                ?.let { inspection -> _uiState.value = inspection.toFormState() }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ─── Field updates ────────────────────────────────────────────────────────

    fun onDateChange(date: LocalDate) = _uiState.update { it.copy(date = date) }

    fun onQueenSeenChange(value: Boolean) = _uiState.update { it.copy(queenSeen = value) }

    fun onBroodSeenChange(value: Boolean) = _uiState.update { it.copy(broodSeen = value) }

    fun onQueenCellsSeenChange(value: Boolean) = _uiState.update { it.copy(queenCellsSeen = value) }

    fun onColonyStrengthChange(sliderIndex: Int) {
        val strength = ColonyStrength.entries[sliderIndex.coerceIn(0, ColonyStrength.entries.lastIndex)]
        _uiState.update { it.copy(colonyStrength = strength) }
    }

    fun onFramesManagementToggle(enabled: Boolean) =
        _uiState.update { it.copy(framesManagementEnabled = enabled) }

    fun onSuperboxesAddedChange(value: Int) =
        _uiState.update { it.copy(superboxesAdded = value.coerceAtLeast(0)) }

    fun onSuperboxesRemovedChange(value: Int) =
        _uiState.update { it.copy(superboxesRemoved = value.coerceAtLeast(0)) }

    fun onDryCombFramesChange(value: Int) =
        _uiState.update { it.copy(dryCombFrames = value.coerceAtLeast(0)) }

    fun onFoundationFramesChange(value: Int) =
        _uiState.update { it.copy(foundationFrames = value.coerceAtLeast(0)) }

    fun onProblemsChange(value: String) = _uiState.update { it.copy(problems = value) }

    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    // ─── Photos ───────────────────────────────────────────────────────────────

    fun onPhotoAdded(path: String) {
        _uiState.update { it.copy(photoPaths = it.photoPaths + path) }
    }

    fun onPhotoRemoved(path: String) {
        _uiState.update { it.copy(photoPaths = it.photoPaths - path) }
    }

    fun onPhotosFromGallery(uris: List<android.net.Uri>) {
        _uiState.update { it.copy(photoPaths = it.photoPaths + uris.map { uri -> uri.toString() }) }
    }

    // ─── QR code ──────────────────────────────────────────────────────────────

    fun onGenerateQrClick() {
        viewModelScope.launch {
            runCatching {
                generateQrBitmap("hive:${hiveId}", 512)
            }.onSuccess { bitmap ->
                _uiState.update { it.copy(showQrDialog = true, qrBitmap = bitmap) }
            }.onFailure { e ->
                _events.send(InspectionFormEvent.ShowMessage("Błąd generowania QR: ${e.message}"))
            }
        }
    }

    fun onDismissQrDialog() {
        _uiState.update { it.copy(showQrDialog = false) }
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    fun onSaveClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { saveInspectionUseCase(uiState.value.toInspection()) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InspectionFormEvent.NavigateBack)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InspectionFormEvent.ShowMessage("Błąd zapisu: ${e.message}"))
                }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(InspectionFormEvent.NavigateBack) }
    }
}
