package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Hive(
    val id: Long = 0,
    val apiaryId: Long,
    val number: Int,
    val name: String,
    val queenYear: Int? = null,
    val status: HiveStatus = HiveStatus.ACTIVE,
    val frameType: String = "Langstroth",
    val superboxCount: Int = 0,
    val queenOrigin: String = "",
    val notes: String = "",
    val installedAt: LocalDate = LocalDate.now(),
    val qrCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

enum class HiveStatus {
    ACTIVE,
    WEAK,
    DEAD;

    fun displayName(): String = when (this) {
        ACTIVE -> "Aktywny"
        WEAK   -> "Słaby"
        DEAD   -> "Martwy"
    }
}
