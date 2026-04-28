package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.HivePhotoDao
import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.domain.model.HivePhoto
import com.example.apiarymanager.domain.repository.HivePhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class HivePhotoRepositoryImpl @Inject constructor(
    private val dao: HivePhotoDao
) : HivePhotoRepository {

    override fun getPhotosByHive(hiveId: Long): Flow<List<HivePhoto>> =
        dao.getPhotosByHive(hiveId).map { it.map(HivePhotoEntity::toDomain) }

    override suspend fun syncPhotosForInspection(hiveId: Long, inspectionId: Long, paths: List<String>) {
        val existing = dao.getPhotosByInspectionOnce(inspectionId)
        val existingPaths = existing.map { it.localPath }.toSet()
        val newPaths = paths.toSet()

        val toInsert = (newPaths - existingPaths).map { path ->
            HivePhotoEntity(
                hiveId       = hiveId,
                inspectionId = inspectionId,
                localPath    = path,
                createdAt    = LocalDateTime.now().toString()
            )
        }
        if (toInsert.isNotEmpty()) dao.insertPhotos(toInsert)

        existing
            .filter { it.localPath !in newPaths }
            .forEach { dao.deletePhotoById(it.id) }
    }

    override suspend fun deletePhoto(id: Long) = dao.deletePhotoById(id)
}
