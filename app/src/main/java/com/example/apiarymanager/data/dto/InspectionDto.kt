package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InspectionDto(
    val id: Long,
    val hiveId: Long,
    val date: String,
    val queenSeen: Boolean = false,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    val colonyStrength: String = "NORMAL",
    val broodCondition: String = "GOOD",
    val honeyStoresKg: Float = 0f,
    val frameCount: Int = 0,
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    val problems: String = "",
    val notes: String = "",
    val newQueenYear: Int? = null
)

@Serializable
data class CreateInspectionRequest(
    val hiveId: Long,
    val date: String,
    val queenSeen: Boolean = false,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    val colonyStrength: String = "NORMAL",
    val broodCondition: String = "GOOD",
    val honeyStoresKg: Float = 0f,
    val frameCount: Int = 0,
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    val problems: String = "",
    val notes: String = "",
    val newQueenYear: Int? = null,
    val photoIds: List<Long> = emptyList()
)

@Serializable
data class UpdateInspectionRequest(
    val date: String,
    val queenSeen: Boolean = false,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    val colonyStrength: String = "NORMAL",
    val broodCondition: String = "GOOD",
    val honeyStoresKg: Float = 0f,
    val frameCount: Int = 0,
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    val problems: String = "",
    val notes: String = "",
    val newQueenYear: Int? = null,
    val photoIds: List<Long> = emptyList()
)
