package com.example.apiarymanager.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.repository.HiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HivesMapViewModel @Inject constructor(
    hiveRepository: HiveRepository
) : ViewModel() {

    val uiState: StateFlow<HivesMapUiState> = hiveRepository.getAllHives()
        .map { hives -> HivesMapUiState(isLoading = false, hives = hives) }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = HivesMapUiState(isLoading = true)
        )
}
