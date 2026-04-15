package com.example.apiarymanager.presentation.feeding

data class FeedingFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val foodType: String = "",
    val weightKg: String = "",
    val applicationMethod: String = "",
    val weightError: String? = null
)

sealed interface FeedingFormEvent {
    data object NavigateBack : FeedingFormEvent
}
