package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Feeding(
    val id: Long = 0,
    val hiveId: Long,
    val date: LocalDate = LocalDate.now(),
    val foodType: String = "",
    val weightKg: Float = 0f,
    val applicationMethod: String = ""
)
