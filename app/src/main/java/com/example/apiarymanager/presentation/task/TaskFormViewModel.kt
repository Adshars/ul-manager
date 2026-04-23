package com.example.apiarymanager.presentation.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.TaskPriority
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.TaskRepository
import com.example.apiarymanager.presentation.navigation.TaskFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository
) : ViewModel() {

    private val route: TaskFormRoute = savedStateHandle.toRoute()
    private val taskId = route.taskId

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<TaskFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val apiaries = apiaryRepository.getAllApiaries().first()
            _uiState.update { it.copy(allApiaries = apiaries) }

            // Pre-fill scope from route params
            when {
                route.hiveId != null -> {
                    _uiState.update { it.copy(scope = TaskScope.HIVE, selectedHiveId = route.hiveId) }
                    val hive = hiveRepository.getHiveById(route.hiveId).first()
                    hive?.let {
                        _uiState.update { s -> s.copy(selectedApiaryId = it.apiaryId) }
                        loadHivesForApiary(it.apiaryId)
                    }
                }
                route.apiaryId != null -> {
                    _uiState.update { it.copy(scope = TaskScope.APIARY, selectedApiaryId = route.apiaryId) }
                    loadHivesForApiary(route.apiaryId)
                }
            }

            taskId?.let { loadTask(it) }
        }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            taskRepository.getTaskById(id).first()?.let { task ->
                val scope = when {
                    task.hiveId   != null -> TaskScope.HIVE
                    task.apiaryId != null -> TaskScope.APIARY
                    else                  -> TaskScope.GENERAL
                }
                _uiState.update {
                    it.copy(
                        title       = task.title,
                        description = task.description,
                        scope       = scope,
                        dueDate     = task.dueDate,
                        priority    = task.priority.name,
                        selectedApiaryId = task.apiaryId,
                        selectedHiveId   = task.hiveId
                    )
                }
                task.apiaryId?.let { loadHivesForApiary(it) }
            }
        }
    }

    private fun loadHivesForApiary(apiaryId: Long) {
        viewModelScope.launch {
            val hives = hiveRepository.getHivesByApiary(apiaryId).first()
            _uiState.update { it.copy(hivesForApiary = hives) }
        }
    }

    fun onTitleChange(v: String)       { _uiState.update { it.copy(title = v, titleError = null) } }
    fun onDescriptionChange(v: String) { _uiState.update { it.copy(description = v) } }
    fun onDueDateChange(v: LocalDate?) { _uiState.update { it.copy(dueDate = v) } }
    fun onPriorityChange(v: String)    { _uiState.update { it.copy(priority = v) } }
    fun onBackClick()                  { viewModelScope.launch { _events.send(TaskFormEvent.NavigateBack) } }

    fun onScopeChange(scope: TaskScope) {
        _uiState.update { it.copy(scope = scope, selectedApiaryId = null, selectedHiveId = null) }
    }

    fun onApiarySelected(apiaryId: Long) {
        _uiState.update { it.copy(selectedApiaryId = apiaryId, selectedHiveId = null) }
        loadHivesForApiary(apiaryId)
    }

    fun onHiveSelected(hiveId: Long) {
        _uiState.update { it.copy(selectedHiveId = hiveId) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Nazwa jest wymagana") }
            return
        }
        val task = Task(
            id          = taskId ?: 0,
            title       = state.title.trim(),
            description = state.description.trim(),
            dueDate     = state.dueDate,
            priority    = runCatching { TaskPriority.valueOf(state.priority) }.getOrDefault(TaskPriority.MEDIUM),
            apiaryId    = if (state.scope == TaskScope.APIARY || state.scope == TaskScope.HIVE) state.selectedApiaryId else null,
            hiveId      = if (state.scope == TaskScope.HIVE) state.selectedHiveId else null
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (taskId == null) taskRepository.insertTask(task)
            else                taskRepository.updateTask(task)
            _events.send(TaskFormEvent.NavigateBack)
        }
    }
}
