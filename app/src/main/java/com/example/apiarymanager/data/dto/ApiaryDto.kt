package com.example.apiarymanager.data.dto

/**
 * DTO representing the API contract for an Apiary.
 * Field names match what the C# backend will return.
 * When Retrofit is added, annotate with @SerializedName / @Json.
 */
data class ApiaryDto(
    val id: Long,
    val name: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val notes: String,
    val createdAt: String   // ISO-8601: "2024-04-01"
)
