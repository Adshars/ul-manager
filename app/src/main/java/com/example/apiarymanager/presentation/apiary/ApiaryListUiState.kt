package com.example.apiarymanager.presentation.apiary

import com.example.apiarymanager.domain.model.Apiary

data class ApiaryWithCount(
    val apiary: Apiary,
    val hiveCount: Int
)

data class ApiaryListUiState(
    val apiaries: List<ApiaryWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val deleteConfirmId: Long? = null
)

sealed interface ApiaryListEvent {
    data class NavigateToHiveList(val apiaryId: Long) : ApiaryListEvent
    data class NavigateToApiaryForm(val apiaryId: Long?) : ApiaryListEvent
    data class ShowMessage(val message: String) : ApiaryListEvent
}
