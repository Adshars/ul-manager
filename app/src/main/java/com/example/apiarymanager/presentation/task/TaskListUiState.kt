package com.example.apiarymanager.presentation.task

import com.example.apiarymanager.domain.model.Task
import java.time.LocalDate
import java.time.YearMonth

enum class TaskListView { LIST, CALENDAR }

enum class TaskListFilter { ALL, PENDING, COMPLETED }

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val activeView: TaskListView = TaskListView.LIST,
    val activeFilter: TaskListFilter = TaskListFilter.ALL,
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true
)

sealed interface TaskListEvent {
    data class NavigateToTaskForm(val taskId: Long? = null) : TaskListEvent
}

// ── Computed helpers ──────────────────────────────────────────────────────────

fun TaskListUiState.filteredTasks(): List<Task> = when (activeFilter) {
    TaskListFilter.ALL       -> tasks
    TaskListFilter.PENDING   -> tasks.filter { !it.isCompleted }
    TaskListFilter.COMPLETED -> tasks.filter { it.isCompleted }
}

/** Tasks grouped for the LIST view (ignores completed group when filter = PENDING). */
fun TaskListUiState.groupedTasks(): Map<TaskGroup, List<Task>> {
    val source = filteredTasks()
    return buildMap {
        if (activeFilter != TaskListFilter.COMPLETED) {
            val overdue = source.filter { !it.isCompleted && it.dueDate != null && it.dueDate.isBefore(today) }
                .sortedBy { it.dueDate }
            val todayTasks = source.filter { !it.isCompleted && it.dueDate == today }
            val upcoming = source.filter { !it.isCompleted && (it.dueDate == null || it.dueDate.isAfter(today)) }
                .sortedWith(compareBy(nullsLast()) { it.dueDate })
            if (overdue.isNotEmpty())   put(TaskGroup.OVERDUE, overdue)
            if (todayTasks.isNotEmpty()) put(TaskGroup.TODAY, todayTasks)
            if (upcoming.isNotEmpty())  put(TaskGroup.UPCOMING, upcoming)
        }
        if (activeFilter != TaskListFilter.PENDING) {
            val done = source.filter { it.isCompleted }.sortedByDescending { it.dueDate }
            if (done.isNotEmpty()) put(TaskGroup.DONE, done)
        }
    }
}

enum class TaskGroup(val label: String) {
    OVERDUE("Zaległe"),
    TODAY("Dzisiaj"),
    UPCOMING("Nadchodzące"),
    DONE("Ukończone")
}

fun TaskListUiState.tasksForSelectedDate(): List<Task> =
    tasks.filter { it.dueDate == selectedDate }

fun TaskListUiState.datesWithTasks(): Set<LocalDate> =
    tasks.mapNotNull { it.dueDate }.toSet()
