package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.HivePhotoDto
import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import com.example.apiarymanager.domain.model.HivePhoto
import com.example.apiarymanager.domain.model.UploadStatus

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun HivePhotoDto.toDomain(): HivePhoto = HivePhoto(
    id           = id,
    hiveId       = hiveId,
    inspectionId = inspectionId,
    localPath    = downloadUrl,
    remoteId     = id,
    uploadStatus = UploadStatus.UPLOADED,
    createdAt    = createdAt
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun HivePhotoEntity.toDomain() = HivePhoto(
    id           = id,
    hiveId       = hiveId,
    inspectionId = inspectionId,
    localPath    = localPath,
    remoteId     = remoteId,
    uploadStatus = runCatching { UploadStatus.valueOf(uploadStatus) }.getOrDefault(UploadStatus.PENDING),
    createdAt    = createdAt
)
