package com.example.apiarymanager.presentation.aianalysis

import com.example.apiarymanager.domain.model.AnalysisType
import com.example.apiarymanager.domain.model.HivePhoto

enum class AiAnalysisStep { SELECT_PHOTO, FORM, ANALYZING, RESULT }

data class AiAnalysisUiState(
    val step: AiAnalysisStep = AiAnalysisStep.SELECT_PHOTO,
    val photos: List<HivePhoto> = emptyList(),
    val selectedPhoto: HivePhoto? = null,
    val note: String = "",
    val analysisType: AnalysisType = AnalysisType.FIND_QUEEN,
    val resultMessage: String = "",
    val isSuccess: Boolean = false,
    val isUploading: Boolean = false
)

sealed interface AiAnalysisEvent {
    data object NavigateBack : AiAnalysisEvent
}
