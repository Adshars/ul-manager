package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val apiaries: List<DashboardApiaryDto> = emptyList(),
    val pendingTasks: List<TaskDto> = emptyList()
)

@Serializable
data class DashboardApiaryDto(
    val apiary: ApiaryDto,
    val activeHiveCount: Int
)
