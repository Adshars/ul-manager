package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiaryDto(
    val id: Long,
    val name: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val createdAt: String = ""
)

@Serializable
data class CreateApiaryRequest(
    val name: String,
    val location: String,
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class UpdateApiaryRequest(
    val name: String,
    val location: String,
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
