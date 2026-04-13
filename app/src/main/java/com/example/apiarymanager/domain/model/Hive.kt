package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Hive(
    val id: Long = 0,
    val apiaryId: Long,
    val number: Int,
    val name: String,
    val queenYear: Int? = null,
    val status: HiveStatus = HiveStatus.ACTIVE,
    val notes: String = "",
    val installedAt: LocalDate = LocalDate.now()
)

enum class HiveStatus {
    ACTIVE,
    WEAK,
    DEAD,
    SOLD;

    fun displayName(): String = when (this) {
        ACTIVE -> "Aktywny"
        WEAK   -> "Słaby"
        DEAD   -> "Martwy"
        SOLD   -> "Sprzedany"
    }
}
