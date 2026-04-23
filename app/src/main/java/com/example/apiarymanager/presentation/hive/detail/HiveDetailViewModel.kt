package com.example.apiarymanager.presentation.hive.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.Treatment
import com.example.apiarymanager.domain.repository.FeedingRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import com.example.apiarymanager.domain.repository.InspectionRepository
import com.example.apiarymanager.domain.repository.TaskRepository
import com.example.apiarymanager.domain.repository.TreatmentRepository
import com.example.apiarymanager.presentation.navigation.HiveDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HiveDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository,
    private val inspectionRepository: InspectionRepository,
    private val harvestRepository: HoneyHarvestRepository,
    private val treatmentRepository: TreatmentRepository,
    private val feedingRepository: FeedingRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val route: HiveDetailRoute = savedStateHandle.toRoute()
    private val hiveId = route.hiveId

    private val _events = Channel<HiveDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Combine activities into one intermediate flow
    private val activitiesFlow = combine(
        inspectionRepository.getInspectionsByHive(hiveId),
        harvestRepository.getHarvestsByHive(hiveId),
        treatmentRepository.getTreatmentsByHive(hiveId),
        feedingRepository.getFeedingsByHive(hiveId),
        taskRepository.getTasksByHive(hiveId)
    ) { inspections, harvests, treatments, feedings, tasks ->
        Activities(inspections, harvests, treatments, feedings, tasks)
    }

    val uiState: StateFlow<HiveDetailUiState> = combine(
        hiveRepository.getHiveById(hiveId),
        activitiesFlow
    ) { hive, activities ->
        HiveDetailUiState(
            isLoading   = false,
            hive        = hive,
            inspections = activities.inspections,
            harvests    = activities.harvests,
            treatments  = activities.treatments,
            feedings    = activities.feedings,
            tasks       = activities.tasks
        )
    }
    .onStart { emit(HiveDetailUiState(isLoading = true)) }
    .catch { e -> emit(HiveDetailUiState(isLoading = false, errorMessage = e.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HiveDetailUiState())

    // ─── Navigation ───────────────────────────────────────────────────────────

    fun onBackClick() {
        viewModelScope.launch { _events.send(HiveDetailEvent.NavigateBack) }
    }

    fun onEditHiveClick() {
        val apiary = uiState.value.hive?.apiaryId ?: return
        viewModelScope.launch { _events.send(HiveDetailEvent.NavigateToHiveForm(apiary, hiveId)) }
    }

    fun onViewQrClick() { send(HiveDetailEvent.NavigateToHiveQr(hiveId)) }

    fun onAddInspection()          { send(HiveDetailEvent.NavigateToInspectionForm(hiveId, null)) }
    fun onEditInspection(id: Long) { send(HiveDetailEvent.NavigateToInspectionForm(hiveId, id))  }
    fun onDeleteInspection(id: Long) {
        viewModelScope.launch {
            inspectionRepository.deleteInspection(id)
            _events.send(HiveDetailEvent.ShowMessage("Przegląd usunięty"))
        }
    }

    fun onAddHarvest()             { send(HiveDetailEvent.NavigateToHarvestForm(hiveId, null))    }
    fun onEditHarvest(id: Long)    { send(HiveDetailEvent.NavigateToHarvestForm(hiveId, id))      }
    fun onDeleteHarvest(id: Long)  { viewModelScope.launch { harvestRepository.deleteHarvest(id)  } }

    fun onAddTreatment()           { send(HiveDetailEvent.NavigateToTreatmentForm(hiveId, null))  }
    fun onEditTreatment(id: Long)  { send(HiveDetailEvent.NavigateToTreatmentForm(hiveId, id))    }
    fun onDeleteTreatment(id: Long){ viewModelScope.launch { treatmentRepository.deleteTreatment(id) } }

    fun onAddFeeding()             { send(HiveDetailEvent.NavigateToFeedingForm(hiveId, null))    }
    fun onEditFeeding(id: Long)    { send(HiveDetailEvent.NavigateToFeedingForm(hiveId, id))      }
    fun onDeleteFeeding(id: Long)  { viewModelScope.launch { feedingRepository.deleteFeeding(id)  } }

    fun onAddTask()                { send(HiveDetailEvent.NavigateToTaskForm(hiveId, null))       }
    fun onEditTask(id: Long)       { send(HiveDetailEvent.NavigateToTaskForm(hiveId, id))         }
    fun onTaskCheckedChange(id: Long, done: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(id, done) }
    }

    private fun send(event: HiveDetailEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    // ─── Internal aggregation type ────────────────────────────────────────────

    private data class Activities(
        val inspections: List<Inspection>,
        val harvests: List<HoneyHarvest>,
        val treatments: List<Treatment>,
        val feedings: List<Feeding>,
        val tasks: List<Task>
    )
}
