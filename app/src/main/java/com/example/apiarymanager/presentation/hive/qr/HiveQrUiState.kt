package com.example.apiarymanager.presentation.hive.qr

import android.graphics.Bitmap

data class HiveQrUiState(
    val isLoading: Boolean = true,
    val hiveName: String = "",
    val qrCode: String = "",
    val qrBitmap: Bitmap? = null,
    val showRegenerateConfirm: Boolean = false,
    val isRegenerating: Boolean = false
)

sealed interface HiveQrEvent {
    data class SendEmail(val bitmap: Bitmap, val hiveName: String) : HiveQrEvent
    data object NavigateBack : HiveQrEvent
    data class ShowMessage(val message: String) : HiveQrEvent
}
