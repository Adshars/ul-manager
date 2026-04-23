package com.example.apiarymanager.presentation.dashboard

import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.Task

/** Apiary + its active hive count — used only in the Dashboard layer. */
data class DashboardApiary(
    val apiary: Apiary,
    val activeHiveCount: Int
)

data class HivePickerState(
    val isOpen: Boolean = false,
    val action: QuickActionType? = null,
    val selectedApiary: Apiary? = null,
    val hives: List<Hive> = emptyList(),
    val isLoadingHives: Boolean = false
)

data class DashboardUiState(
    val apiaries: List<DashboardApiary> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val hivePicker: HivePickerState = HivePickerState(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

enum class QuickActionType {
    NEW_INSPECTION,
    HARVEST,
    ADD_TASK,
    MAP
}

sealed interface DashboardEvent {
    data class NavigateToHiveList(val apiaryId: Long) : DashboardEvent
    data object NavigateToTaskForm : DashboardEvent
    data class NavigateToInspectionForm(val hiveId: Long) : DashboardEvent
    data class NavigateToHarvestForm(val hiveId: Long) : DashboardEvent
    data class ShowMessage(val message: String) : DashboardEvent
}
