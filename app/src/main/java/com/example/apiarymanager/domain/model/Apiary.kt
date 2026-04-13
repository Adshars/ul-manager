package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Apiary(
    val id: Long = 0,
    val name: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val createdAt: LocalDate = LocalDate.now()
)
