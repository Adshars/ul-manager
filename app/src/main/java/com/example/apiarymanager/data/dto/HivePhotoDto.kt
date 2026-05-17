package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class HivePhotoDto(
    val id: Long,
    val hiveId: Long,
    val inspectionId: Long? = null,
    val blobPath: String = "",
    val thumbnailBlobPath: String = "",
    val createdAt: String = "",
    val downloadUrl: String = "",
    val thumbnailUrl: String = ""
)

@Serializable
data class AnalyzePhotoRequest(
    val type: String,
    val note: String = ""
)

@Serializable
data class AnalysisResultDto(
    val isSuccess: Boolean,
    val message: String
)
