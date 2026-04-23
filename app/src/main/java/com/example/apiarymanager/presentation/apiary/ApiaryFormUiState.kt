package com.example.apiarymanager.presentation.apiary

import com.example.apiarymanager.domain.model.Apiary

data class ApiaryFormUiState(
    val apiaryId: Long? = null,
    val name: String = "",
    val location: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null
)

sealed interface ApiaryFormEvent {
    data object NavigateBack : ApiaryFormEvent
    data class ShowMessage(val message: String) : ApiaryFormEvent
}

fun ApiaryFormUiState.toApiary(): Apiary = Apiary(
    id       = apiaryId ?: 0L,
    name     = name.trim(),
    location = location.trim(),
    notes    = notes.trim()
)

fun Apiary.toFormState(): ApiaryFormUiState = ApiaryFormUiState(
    apiaryId = id,
    name     = name,
    location = location,
    notes    = notes
)
