package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Inspection(
    val id: Long = 0,
    val hiveId: Long,
    val date: LocalDate = LocalDate.now(),
    // Observations
    val queenSeen: Boolean = false,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    // Colony condition
    val colonyStrength: ColonyStrength = ColonyStrength.NORMAL,
    val broodCondition: BroodCondition = BroodCondition.GOOD,
    val honeyStoresKg: Float = 0f,
    // Frame management
    val frameCount: Int = 0,
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    // Free text
    val problems: String = "",
    val notes: String = ""
)

enum class ColonyStrength {
    CRITICAL,
    WEAK,
    NORMAL,
    STRONG,
    VERY_STRONG;

    fun displayName(): String = when (this) {
        CRITICAL    -> "Krytyczna"
        WEAK        -> "Słaba"
        NORMAL      -> "Normalna"
        STRONG      -> "Silna"
        VERY_STRONG -> "Bardzo Silna"
    }

    fun shortLabel(): String = when (this) {
        CRITICAL    -> "Kryt."
        WEAK        -> "Słaba"
        NORMAL      -> "Norm."
        STRONG      -> "Silna"
        VERY_STRONG -> "B.Silna"
    }
}

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
