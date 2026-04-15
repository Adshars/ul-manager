package com.example.apiarymanager.presentation.hive.detail

import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.Treatment

data class HiveDetailUiState(
    val isLoading: Boolean = true,
    val hive: Hive? = null,
    val inspections: List<Inspection> = emptyList(),
    val harvests: List<HoneyHarvest> = emptyList(),
    val treatments: List<Treatment> = emptyList(),
    val feedings: List<Feeding> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null
)

sealed interface HiveDetailEvent {
    data object NavigateBack : HiveDetailEvent
    data class NavigateToHiveForm(val apiaryId: Long, val hiveId: Long) : HiveDetailEvent
    data class NavigateToInspectionForm(val hiveId: Long, val inspectionId: Long?) : HiveDetailEvent
    data class NavigateToHarvestForm(val hiveId: Long, val harvestId: Long?) : HiveDetailEvent
    data class NavigateToTreatmentForm(val hiveId: Long, val treatmentId: Long?) : HiveDetailEvent
    data class NavigateToFeedingForm(val hiveId: Long, val feedingId: Long?) : HiveDetailEvent
    data class NavigateToTaskForm(val hiveId: Long, val taskId: Long?) : HiveDetailEvent
    data class ShowMessage(val message: String) : HiveDetailEvent
}
