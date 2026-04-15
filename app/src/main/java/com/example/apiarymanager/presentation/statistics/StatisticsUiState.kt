package com.example.apiarymanager.presentation.statistics

import com.example.apiarymanager.domain.model.Apiary

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val apiaries: List<Apiary> = emptyList(),
    val selectedApiaryId: Long? = null,  // null = all apiaries
    val totalHoneyKg: Float = 0f,
    val totalFeedingKg: Float = 0f,
    // Monthly harvest: month (1-12) → kg
    val monthlyHarvest: Map<Int, Float> = emptyMap(),
    // Monthly feeding: month (1-12) → kg
    val monthlyFeeding: Map<Int, Float> = emptyMap()
)
