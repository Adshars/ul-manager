package com.example.apiarymanager.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class HoneyHarvestDto(
    val id: Long,
    val hiveId: Long,
    val date: String,
    val honeyType: String,
    val weightKg: Float,
    val notes: String = ""
)

@Serializable
data class CreateHoneyHarvestRequest(
    val hiveId: Long,
    val date: String,
    val honeyType: String,
    val weightKg: Float,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val notes: String? = null
)

@Serializable
data class UpdateHoneyHarvestRequest(
    val date: String,
    val honeyType: String,
    val weightKg: Float,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val notes: String? = null
)
