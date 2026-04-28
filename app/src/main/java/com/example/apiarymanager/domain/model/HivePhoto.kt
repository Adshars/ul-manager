package com.example.apiarymanager.domain.model

data class HivePhoto(
    val id: Long = 0,
    val hiveId: Long,
    val inspectionId: Long?,
    val localPath: String,
    val createdAt: String
)
