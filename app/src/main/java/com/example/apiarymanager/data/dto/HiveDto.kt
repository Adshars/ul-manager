package com.example.apiarymanager.data.dto

/**
 * DTO representing the API contract for a Hive.
 * [status] mirrors the C# enum name: "ACTIVE" | "WEAK" | "DEAD" | "SOLD"
 */
data class HiveDto(
    val id: Long,
    val apiaryId: Long,
    val number: Int,
    val name: String,
    val queenYear: Int?,
    val status: String,
    val notes: String,
    val installedAt: String  // ISO-8601: "2024-04-01"
)
