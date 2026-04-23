package com.example.apiarymanager.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _events = Channel<TaskListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private data class ViewState(
        val activeView: TaskListView = TaskListView.LIST,
        val activeFilter: TaskListFilter = TaskListFilter.ALL,
        val selectedDate: LocalDate = LocalDate.now(),
        val calendarMonth: YearMonth = YearMonth.now()
    )

    private val _viewState = MutableStateFlow(ViewState())

    val uiState: StateFlow<TaskListUiState> = combine(
        taskRepository.getAllTasks(),
        _viewState
    ) { tasks, vs ->
        TaskListUiState(
            tasks         = tasks,
            activeView    = vs.activeView,
            activeFilter  = vs.activeFilter,
            selectedDate  = vs.selectedDate,
            calendarMonth = vs.calendarMonth,
            today         = LocalDate.now(),
            isLoading     = false
        )
    }
    .onStart { emit(TaskListUiState(isLoading = true)) }
    .catch   { emit(TaskListUiState(isLoading = false)) }
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState(isLoading = true)
    )

    fun onViewToggle(view: TaskListView) =
        _viewState.update { it.copy(activeView = view) }

    fun onFilterChange(filter: TaskListFilter) =
        _viewState.update { it.copy(activeFilter = filter) }

    fun onDateSelected(date: LocalDate) =
        _viewState.update { it.copy(selectedDate = date) }

    fun onPreviousMonth() =
        _viewState.update { it.copy(calendarMonth = it.calendarMonth.minusMonths(1)) }

    fun onNextMonth() =
        _viewState.update { it.copy(calendarMonth = it.calendarMonth.plusMonths(1)) }

    fun onTaskCheckedChange(taskId: Long, completed: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(taskId, completed) }
    }

    fun onAddTaskClick() {
        viewModelScope.launch { _events.send(TaskListEvent.NavigateToTaskForm()) }
    }

    fun onTaskClick(taskId: Long) {
        viewModelScope.launch { _events.send(TaskListEvent.NavigateToTaskForm(taskId)) }
    }
}
