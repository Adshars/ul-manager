package com.example.apiarymanager.presentation.task

import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.model.Hive

enum class TaskScope { GENERAL, APIARY, HIVE }

data class TaskFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val description: String = "",
    val scope: TaskScope = TaskScope.GENERAL,
    val dueDate: java.time.LocalDate? = null,
    val priority: String = "MEDIUM",
    // For scope selection
    val allApiaries: List<Apiary> = emptyList(),
    val hivesForApiary: List<Hive> = emptyList(),
    val selectedApiaryId: Long? = null,
    val selectedHiveId: Long? = null,
    val titleError: String? = null
)

sealed interface TaskFormEvent {
    data object NavigateBack : TaskFormEvent
}
