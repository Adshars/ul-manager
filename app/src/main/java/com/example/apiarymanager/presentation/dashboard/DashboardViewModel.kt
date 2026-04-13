package com.example.apiarymanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
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

    val uiState = combine(
        apiariesWithCountsFlow(),
        pendingTasksFlow()
    ) { apiaries, tasks ->
        DashboardUiState(
            apiaries    = apiaries,
            pendingTasks = tasks,
            isLoading   = false
        )
    }
    .onStart { emit(DashboardUiState(isLoading = true)) }
    .catch { e ->
        emit(DashboardUiState(isLoading = false, errorMessage = e.message))
    }

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
        val message = when (type) {
            QuickActionType.NEW_INSPECTION -> "Wybierz ul z listy pasiek poniżej"
            QuickActionType.HARVEST        -> "Funkcja miodobrania — wkrótce dostępna"
            QuickActionType.ADD_TASK       -> "Formularz zadań — wkrótce dostępny"
            QuickActionType.MAP            -> "Mapa pasiek — wkrótce dostępna"
        }
        viewModelScope.launch { _events.send(DashboardEvent.ShowMessage(message)) }
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
