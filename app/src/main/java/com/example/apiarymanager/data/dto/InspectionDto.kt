package com.example.apiarymanager.data.dto

/**
 * DTO representing the API contract for an Inspection.
 * [broodCondition] mirrors enum: "EXCELLENT" | "GOOD" | "FAIR" | "POOR" | "NONE"
 */
data class InspectionDto(
    val id: Long,
    val hiveId: Long,
    val date: String,           // ISO-8601
    val queenSeen: Boolean,
    val broodCondition: String,
    val honeyStoresKg: Float,
    val frameCount: Int,
    val notes: String
)
