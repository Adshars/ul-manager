package com.example.apiarymanager.presentation.apiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiaryListViewModel @Inject constructor(
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository
) : ViewModel() {

    private val _events = Channel<ApiaryListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _deleteConfirmId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ApiaryListUiState> = combine(
        apiariesWithCountsFlow(),
        _deleteConfirmId
    ) { apiaries, deleteId ->
        ApiaryListUiState(apiaries = apiaries, isLoading = false, deleteConfirmId = deleteId)
    }
    .onStart { emit(ApiaryListUiState(isLoading = true)) }
    .catch { emit(ApiaryListUiState(isLoading = false)) }
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApiaryListUiState(isLoading = true)
    )

    fun onApiaryClick(apiaryId: Long) {
        viewModelScope.launch { _events.send(ApiaryListEvent.NavigateToHiveList(apiaryId)) }
    }

    fun onAddClick() {
        viewModelScope.launch { _events.send(ApiaryListEvent.NavigateToApiaryForm(null)) }
    }

    fun onEditClick(apiaryId: Long) {
        viewModelScope.launch { _events.send(ApiaryListEvent.NavigateToApiaryForm(apiaryId)) }
    }

    fun onDeleteRequest(apiaryId: Long) {
        _deleteConfirmId.value = apiaryId
    }

    fun onDeleteConfirmed() {
        val id = _deleteConfirmId.value ?: return
        _deleteConfirmId.value = null
        viewModelScope.launch {
            try {
                apiaryRepository.deleteApiary(id)
            } catch (e: Exception) {
                _events.send(ApiaryListEvent.ShowMessage("Błąd podczas usuwania pasieki"))
            }
        }
    }

    fun onDeleteCancelled() {
        _deleteConfirmId.value = null
    }

    private fun apiariesWithCountsFlow(): Flow<List<ApiaryWithCount>> =
        apiaryRepository.getAllApiaries().flatMapLatest { apiaries ->
            if (apiaries.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(
                apiaries.map { apiary ->
                    hiveRepository.getActiveHiveCount(apiary.id).map { count ->
                        ApiaryWithCount(apiary, count)
                    }
                }
            ) { it.toList() }
        }
}
