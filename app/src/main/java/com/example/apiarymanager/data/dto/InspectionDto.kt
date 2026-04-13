package com.example.apiarymanager.data.dto

/**
 * DTO representing the API contract for an Inspection.
 * New fields use default values so existing seeder call-sites compile unchanged.
 *
 * [broodCondition]  — "EXCELLENT" | "GOOD" | "FAIR" | "POOR" | "NONE"
 * [colonyStrength]  — "CRITICAL" | "WEAK" | "NORMAL" | "STRONG" | "VERY_STRONG"
 */
data class InspectionDto(
    val id: Long,
    val hiveId: Long,
    val date: String,               // ISO-8601
    // Observations
    val queenSeen: Boolean,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    // Colony condition
    val broodCondition: String,
    val colonyStrength: String = "NORMAL",
    val honeyStoresKg: Float,
    val frameCount: Int,
    // Frame management
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    // Free text
    val problems: String = "",
    val notes: String
)
