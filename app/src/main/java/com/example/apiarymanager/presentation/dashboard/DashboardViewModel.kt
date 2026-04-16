package com.example.apiarymanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _events = Channel<DashboardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _pickerState = MutableStateFlow(HivePickerState())

    val uiState: StateFlow<DashboardUiState> = combine(
        apiariesWithCountsFlow(),
        pendingTasksFlow(),
        _pickerState
    ) { apiaries, tasks, picker ->
        DashboardUiState(
            apiaries     = apiaries,
            pendingTasks = tasks,
            hivePicker   = picker,
            isLoading    = false
        )
    }
    .onStart { emit(DashboardUiState(isLoading = true)) }
    .catch { e -> emit(DashboardUiState(isLoading = false, errorMessage = e.message)) }
    .stateIn(
        scope            = viewModelScope,
        started          = SharingStarted.WhileSubscribed(5_000),
        initialValue     = DashboardUiState(isLoading = true)
    )

    // ─── Public actions ───────────────────────────────────────────────────────

    fun onApiaryClick(apiaryId: Long) {
        viewModelScope.launch {
            _events.send(DashboardEvent.NavigateToHiveList(apiaryId))
        }
    }

    fun onTaskCheckedChange(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, completed)
        }
    }

    fun onQuickActionClick(type: QuickActionType) {
        viewModelScope.launch {
            when (type) {
                QuickActionType.NEW_INSPECTION,
                QuickActionType.HARVEST -> {
                    if (uiState.value.apiaries.isEmpty()) {
                        _events.send(DashboardEvent.ShowMessage("Najpierw dodaj pasiekę"))
                    } else {
                        _pickerState.update { HivePickerState(isOpen = true, action = type) }
                    }
                }
                QuickActionType.ADD_TASK -> _events.send(DashboardEvent.NavigateToTaskForm)
                QuickActionType.MAP      -> _events.send(DashboardEvent.ShowMessage("Mapa pasiek — wkrótce dostępna"))
            }
        }
    }

    fun onPickerApiarySelected(apiary: Apiary) {
        _pickerState.update { it.copy(selectedApiary = apiary, isLoadingHives = true, hives = emptyList()) }
        viewModelScope.launch {
            try {
                val hives = hiveRepository.getHivesByApiary(apiary.id).first()
                _pickerState.update { it.copy(hives = hives, isLoadingHives = false) }
            } catch (e: Exception) {
                _pickerState.update { it.copy(isLoadingHives = false) }
                _events.send(DashboardEvent.ShowMessage("Błąd podczas wczytywania uli"))
            }
        }
    }

    fun onPickerHiveSelected(hiveId: Long) {
        val action = _pickerState.value.action
        _pickerState.update { HivePickerState() }
        viewModelScope.launch {
            when (action) {
                QuickActionType.NEW_INSPECTION -> _events.send(DashboardEvent.NavigateToInspectionForm(hiveId))
                QuickActionType.HARVEST        -> _events.send(DashboardEvent.NavigateToHarvestForm(hiveId))
                else -> {}
            }
        }
    }

    fun onPickerBackToApiaries() {
        _pickerState.update { it.copy(selectedApiary = null, hives = emptyList()) }
    }

    fun onPickerDismiss() {
        _pickerState.update { HivePickerState() }
    }

    // ─── Private flow builders ────────────────────────────────────────────────

    /**
     * Combines each apiary with its live active-hive count.
     * Uses [combine] over an iterable of flows — re-emits whenever any count changes.
     */
    private fun apiariesWithCountsFlow(): Flow<List<DashboardApiary>> =
        apiaryRepository.getAllApiaries().flatMapLatest { apiaries ->
            if (apiaries.isEmpty()) return@flatMapLatest flowOf(emptyList())

            combine(
                apiaries.map { apiary ->
                    hiveRepository.getActiveHiveCount(apiary.id).map { count ->
                        DashboardApiary(apiary, count)
                    }
                }
            ) { it.toList() }
        }

    /**
     * All tasks with a due date that is today or overdue and not yet completed.
     * Sorted: earliest due date first, then highest priority first.
     */
    private fun pendingTasksFlow(): Flow<List<Task>> =
        taskRepository.getAllTasks().map { tasks ->
            val today = LocalDate.now()
            tasks
                .filter { it.dueDate != null && !it.dueDate.isAfter(today) && !it.isCompleted }
                .sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.priority.ordinal })
        }
}
