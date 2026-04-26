package com.example.apiarymanager.presentation.hive.location

data class HiveLocationUiState(
    val isLoading: Boolean = true,
    val hiveName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSaving: Boolean = false
)

sealed interface HiveLocationEvent {
    data object NavigateBack : HiveLocationEvent
    data class ShowMessage(val message: String) : HiveLocationEvent
}
