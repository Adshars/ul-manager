package com.example.apiarymanager.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class FeedingDto(
    val id: Long,
    val hiveId: Long,
    val date: String,
    val foodType: String,
    val weightKg: Float,
    val applicationMethod: String = ""
)

@Serializable
data class CreateFeedingRequest(
    val hiveId: Long,
    val date: String,
    val foodType: String,
    val weightKg: Float,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val applicationMethod: String? = null
)

@Serializable
data class UpdateFeedingRequest(
    val date: String,
    val foodType: String,
    val weightKg: Float,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val applicationMethod: String? = null
)
