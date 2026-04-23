package com.example.apiarymanager.presentation.hive.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.presentation.navigation.HiveQrRoute
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HiveQrViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository
) : ViewModel() {

    private val hiveId = savedStateHandle.toRoute<HiveQrRoute>().hiveId

    private val _uiState = MutableStateFlow(HiveQrUiState())
    val uiState: StateFlow<HiveQrUiState> = _uiState.asStateFlow()

    private val _events = Channel<HiveQrEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadHive()
    }

    private fun loadHive() {
        viewModelScope.launch {
            runCatching {
                var hive = hiveRepository.getHiveById(hiveId).first()
                    ?: error("Hive not found")
                // Hives created before QR feature have an empty qrCode — assign one now
                if (hive.qrCode.isBlank()) {
                    hive = hive.copy(qrCode = UUID.randomUUID().toString())
                    hiveRepository.updateHive(hive)
                }
                hive to generateQrBitmap(hive.qrCode, 512)
            }
                .onSuccess { (hive, bitmap) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hiveName  = hive.name,
                            qrCode    = hive.qrCode,
                            qrBitmap  = bitmap
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(HiveQrEvent.ShowMessage("Nie można załadować kodu QR: ${e.message}"))
                }
        }
    }

    fun onSendEmailClick() {
        val state = _uiState.value
        val bitmap = state.qrBitmap ?: return
        viewModelScope.launch {
            _events.send(HiveQrEvent.SendEmail(bitmap, state.hiveName))
        }
    }

    fun onRegenerateRequest() {
        _uiState.update { it.copy(showRegenerateConfirm = true) }
    }

    fun onRegenerateCancelled() {
        _uiState.update { it.copy(showRegenerateConfirm = false) }
    }

    fun onRegenerateConfirmed() {
        _uiState.update { it.copy(showRegenerateConfirm = false, isRegenerating = true) }
        viewModelScope.launch {
            runCatching {
                val hive = hiveRepository.getHiveById(hiveId).first()
                    ?: error("Hive not found")
                val newCode = UUID.randomUUID().toString()
                hiveRepository.updateHive(hive.copy(qrCode = newCode))
                newCode to generateQrBitmap(newCode, 512)
            }
                .onSuccess { (newCode, bitmap) ->
                    _uiState.update {
                        it.copy(
                            isRegenerating = false,
                            qrCode         = newCode,
                            qrBitmap       = bitmap
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isRegenerating = false) }
                    _events.send(HiveQrEvent.ShowMessage("Błąd regeneracji: ${e.message}"))
                }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(HiveQrEvent.NavigateBack) }
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
}
