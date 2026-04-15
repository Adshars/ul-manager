package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Treatment(
    val id: Long = 0,
    val hiveId: Long,
    val date: LocalDate = LocalDate.now(),
    val medicineType: String = "",
    val dosage: String = "",
    val applicationMethod: String = "",
    val mortalityAfterTreatment: String = ""
)
