package com.example.apiarymanager.domain.model

enum class UploadStatus { PENDING, UPLOADED }

data class HivePhoto(
    val id: Long = 0,
    val hiveId: Long,
    val inspectionId: Long?,
    val localPath: String,
    val remoteId: Long? = null,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val createdAt: String
)
