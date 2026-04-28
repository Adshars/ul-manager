package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import com.example.apiarymanager.domain.model.HivePhoto

fun HivePhotoEntity.toDomain() = HivePhoto(
    id           = id,
    hiveId       = hiveId,
    inspectionId = inspectionId,
    localPath    = localPath,
    createdAt    = createdAt
)
