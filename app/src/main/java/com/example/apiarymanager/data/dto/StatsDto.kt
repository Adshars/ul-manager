package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatYearTotalsDto(
    val year: Int,
    val totalKg: Float,
    val byMonth: List<StatMonthDto> = emptyList()
)

@Serializable
data class StatMonthDto(
    val month: Int,
    val kg: Float
)
