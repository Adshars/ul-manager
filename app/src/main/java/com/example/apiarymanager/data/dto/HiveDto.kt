package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

/** [status] mirrors the C# enum name: "ACTIVE" | "WEAK" | "DEAD" */
@Serializable
data class HiveDto(
    val id: Long,
    val apiaryId: Long,
    val number: Int,
    val name: String,
    val queenYear: Int? = null,
    val status: String = "ACTIVE",
    val frameType: String = "Langstroth",
    val superboxCount: Int = 0,
    val queenOrigin: String = "",
    val notes: String = "",
    val installedAt: String = "",
    val qrCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CreateHiveRequest(
    val apiaryId: Long,
    val number: Int,
    val name: String,
    val status: String = "ACTIVE",
    val installedAt: String,
    val queenYear: Int? = null,
    val frameType: String = "Langstroth",
    val superboxCount: Int = 0,
    val queenOrigin: String = "",
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class UpdateHiveRequest(
    val number: Int,
    val name: String,
    val status: String,
    val installedAt: String,
    val queenYear: Int? = null,
    val frameType: String = "Langstroth",
    val superboxCount: Int = 0,
    val queenOrigin: String = "",
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
