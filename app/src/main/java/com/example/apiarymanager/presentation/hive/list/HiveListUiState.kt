package com.example.apiarymanager.presentation.hive.list

import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.model.Hive

data class HiveListUiState(
    val isLoading: Boolean = true,
    val apiary: Apiary? = null,
    val hives: List<Hive> = emptyList(),
    val allApiaries: List<Apiary> = emptyList(),
    val errorMessage: String? = null,
    // Move hive dialog
    val moveDialogHive: Hive? = null,
    val moveTargetApiaryId: Long? = null
)

sealed interface HiveListEvent {
    data class NavigateToHiveDetail(val hiveId: Long) : HiveListEvent
    data class NavigateToHiveForm(val apiaryId: Long, val hiveId: Long?) : HiveListEvent
    data object NavigateBack : HiveListEvent
    data class ShowMessage(val message: String) : HiveListEvent
}
