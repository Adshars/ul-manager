package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Inspection(
    val id: Long = 0,
    val hiveId: Long,
    val date: LocalDate = LocalDate.now(),
    val queenSeen: Boolean = false,
    val broodCondition: BroodCondition = BroodCondition.GOOD,
    val honeyStoresKg: Float = 0f,
    val frameCount: Int = 0,
    val notes: String = ""
)

enum class BroodCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    NONE;

    fun displayName(): String = when (this) {
        EXCELLENT -> "Doskonały"
        GOOD      -> "Dobry"
        FAIR      -> "Przeciętny"
        POOR      -> "Słaby"
        NONE      -> "Brak"
    }
}
