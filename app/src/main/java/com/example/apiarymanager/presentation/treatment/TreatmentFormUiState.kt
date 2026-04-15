package com.example.apiarymanager.presentation.treatment

data class TreatmentFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val medicineType: String = "",
    val dosage: String = "",
    val applicationMethod: String = "",
    val mortalityAfterTreatment: String = ""
)

sealed interface TreatmentFormEvent {
    data object NavigateBack : TreatmentFormEvent
}
