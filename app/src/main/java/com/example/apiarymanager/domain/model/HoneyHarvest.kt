package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class HoneyHarvest(
    val id: Long = 0,
    val hiveId: Long,
    val date: LocalDate = LocalDate.now(),
    val honeyType: String = "",
    val weightKg: Float = 0f,
    val notes: String = ""
)
