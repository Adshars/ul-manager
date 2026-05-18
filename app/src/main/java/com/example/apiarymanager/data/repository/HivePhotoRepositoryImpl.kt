package com.example.apiarymanager.data.repository

import android.net.Uri
import com.example.apiarymanager.data.local.dao.HivePhotoDao
import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.remote.source.HivePhotoSource
import com.example.apiarymanager.domain.model.HivePhoto
import com.example.apiarymanager.domain.repository.HivePhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

class HivePhotoRepositoryImpl @Inject constructor(
    private val dao: HivePhotoDao,
    private val hivePhotoSource: HivePhotoSource
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

    override suspend fun uploadPendingPhotos() {
        dao.getPendingPhotos().forEach { photo ->
            val filePath = if (photo.localPath.startsWith("file://")) {
                Uri.parse(photo.localPath).path ?: return@forEach
            } else {
                photo.localPath
            }
            val file = File(filePath)
            if (!file.exists()) return@forEach
            val part = MultipartBody.Part.createFormData(
                "file", file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
            hivePhotoSource.upload(photo.hiveId, part, photo.inspectionId)
                .onSuccess { uploaded -> dao.markUploaded(photo.id, uploaded.id) }
        }
    }
}
