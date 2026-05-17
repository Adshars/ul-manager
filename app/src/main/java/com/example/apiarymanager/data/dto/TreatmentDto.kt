package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TreatmentDto(
    val id: Long,
    val hiveId: Long,
    val date: String,
    val medicineType: String,
    val dosage: String = "",
    val applicationMethod: String = "",
    val mortalityAfterTreatment: String = ""
)

@Serializable
data class CreateTreatmentRequest(
    val hiveId: Long,
    val date: String,
    val medicineType: String,
    val dosage: String = "",
    val applicationMethod: String = "",
    val mortalityAfterTreatment: String = ""
)

@Serializable
data class UpdateTreatmentRequest(
    val date: String,
    val medicineType: String,
    val dosage: String = "",
    val applicationMethod: String = "",
    val mortalityAfterTreatment: String = ""
)
