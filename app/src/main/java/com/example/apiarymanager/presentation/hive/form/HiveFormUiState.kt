package com.example.apiarymanager.presentation.hive.form

data class HiveFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val number: String = "",
    val queenYear: String = "",
    val frameType: String = "Langstroth",
    val superboxCount: String = "0",
    val queenOrigin: String = "",
    val status: String = "ACTIVE",
    val notes: String = "",
    // errors
    val nameError: String? = null,
    val numberError: String? = null
)

sealed interface HiveFormEvent {
    data object NavigateBack : HiveFormEvent
    data class ShowMessage(val message: String) : HiveFormEvent
}
