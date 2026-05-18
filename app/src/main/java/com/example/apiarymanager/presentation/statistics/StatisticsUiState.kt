package com.example.apiarymanager.presentation.statistics

import com.example.apiarymanager.domain.model.Apiary

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val apiaries: List<Apiary> = emptyList(),
    val selectedApiaryId: Long? = null,
    val selectedYear: Int? = null,
    val availableYears: List<Int> = emptyList(),
    val totalHoneyKg: Float = 0f,
    val totalFeedingKg: Float = 0f,
    val hiveCount: Int = 0,
    // month (1-12) → kg, sorted ascending
    val monthlyHarvest: Map<Int, Float> = emptyMap(),
    val monthlyFeeding: Map<Int, Float> = emptyMap(),
    // honeyType → kg, sorted descending by weight
    val harvestByHoneyType: Map<String, Float> = emptyMap()
)
