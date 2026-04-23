package com.example.apiarymanager.presentation.harvest

data class HarvestFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val honeyType: String = "",
    val weightKg: String = "",
    val notes: String = "",
    val weightError: String? = null
)

sealed interface HarvestFormEvent {
    data object NavigateBack : HarvestFormEvent
}
