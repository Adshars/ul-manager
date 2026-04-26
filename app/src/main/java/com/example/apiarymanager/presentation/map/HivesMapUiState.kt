package com.example.apiarymanager.presentation.map

import com.example.apiarymanager.domain.model.Hive

data class HivesMapUiState(
    val isLoading: Boolean = true,
    val hives: List<Hive> = emptyList()
)
