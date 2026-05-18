package com.example.apiarymanager.data.dto

import kotlinx.serialization.EncodeDefault
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
    @EncodeDefault(EncodeDefault.Mode.NEVER) val notes: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val latitude: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val longitude: Double? = null
)

@Serializable
data class UpdateApiaryRequest(
    val name: String,
    val location: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val notes: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val latitude: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val longitude: Double? = null
)
