package com.example.apiarymanager.presentation.hive.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.presentation.navigation.HiveListRoute
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
class HiveListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository,
    private val apiaryRepository: ApiaryRepository
) : ViewModel() {

    private val route: HiveListRoute = savedStateHandle.toRoute()
    private val apiaryId = route.apiaryId

    private val _events = Channel<HiveListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _moveDialogHive = kotlinx.coroutines.flow.MutableStateFlow<Hive?>(null)
    private val _moveTargetId   = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)

    val uiState: StateFlow<HiveListUiState> = combine(
        apiaryRepository.getApiaryById(apiaryId),
        hiveRepository.getHivesByApiary(apiaryId),
        apiaryRepository.getAllApiaries(),
        _moveDialogHive,
        _moveTargetId
    ) { apiary, hives, allApiaries, moveHive, moveTarget ->
        HiveListUiState(
            isLoading      = false,
            apiary         = apiary,
            hives          = hives,
            allApiaries    = allApiaries,
            moveDialogHive = moveHive,
            moveTargetApiaryId = moveTarget
        )
    }
    .onStart { emit(HiveListUiState(isLoading = true)) }
    .catch { e -> emit(HiveListUiState(isLoading = false, errorMessage = e.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HiveListUiState())

    fun onHiveClick(hiveId: Long) {
        viewModelScope.launch { _events.send(HiveListEvent.NavigateToHiveDetail(hiveId)) }
    }

    fun onAddHiveClick() {
        viewModelScope.launch { _events.send(HiveListEvent.NavigateToHiveForm(apiaryId, null)) }
    }

    fun onEditHiveClick(hiveId: Long) {
        viewModelScope.launch { _events.send(HiveListEvent.NavigateToHiveForm(apiaryId, hiveId)) }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(HiveListEvent.NavigateBack) }
    }

    fun onDeleteHive(hiveId: Long) {
        viewModelScope.launch {
            hiveRepository.deleteHive(hiveId)
            _events.send(HiveListEvent.ShowMessage("Ul usunięty"))
        }
    }

    // ── Move hive ──────────────────────────────────────────────────────────────

    fun onMoveHiveRequest(hive: Hive) {
        val allApiaries = uiState.value.allApiaries
        if (allApiaries.size < 2) {
            viewModelScope.launch {
                _events.send(HiveListEvent.ShowMessage("Musisz posiadać co najmniej 2 pasieki"))
            }
            return
        }
        _moveDialogHive.value = hive
        // Pre-select first apiary that is not the current one
        _moveTargetId.value = allApiaries.firstOrNull { it.id != apiaryId }?.id
    }

    fun onMoveTargetSelected(apiaryId: Long) {
        _moveTargetId.value = apiaryId
    }

    fun onMoveDismiss() {
        _moveDialogHive.value = null
        _moveTargetId.value   = null
    }

    fun onMoveConfirm() {
        val hive   = _moveDialogHive.value ?: return
        val target = _moveTargetId.value   ?: return
        viewModelScope.launch {
            hiveRepository.updateHive(hive.copy(apiaryId = target))
            _moveDialogHive.value = null
            _moveTargetId.value   = null
            _events.send(HiveListEvent.ShowMessage("Ul przeniesiony"))
        }
    }
}
