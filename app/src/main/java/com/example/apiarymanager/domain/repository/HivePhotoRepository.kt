package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.HivePhoto
import kotlinx.coroutines.flow.Flow

interface HivePhotoRepository {
    fun getPhotosByHive(hiveId: Long): Flow<List<HivePhoto>>
    suspend fun syncPhotosForInspection(hiveId: Long, inspectionId: Long, paths: List<String>)
    suspend fun deletePhoto(id: Long)
}
