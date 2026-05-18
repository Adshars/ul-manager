package com.example.apiarymanager.domain.model

enum class AnalysisType {
    FIND_QUEEN,
    DETECT_VARROA;

    fun displayName(): String = when (this) {
        FIND_QUEEN    -> "Lokalizacja matki"
        DETECT_VARROA -> "Wykrywanie Varroa"
    }
}

data class AnalysisRequest(
    val photo: HivePhoto,
    val note: String,
    val type: AnalysisType
)

data class AnalysisResult(
    val isSuccess: Boolean,
    val message: String
)
