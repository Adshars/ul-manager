package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.mapper.toCreateRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.mapper.toUpdateRequest
import com.example.apiarymanager.data.remote.source.InspectionSource
import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InspectionRepositoryImpl @Inject constructor(
    private val dao: InspectionDao,
    private val inspectionSource: InspectionSource
) : InspectionRepository {

    override fun getInspectionsByHive(hiveId: Long): Flow<List<Inspection>> =
        dao.getInspectionsByHive(hiveId).map { entities -> entities.map { it.toDomain() } }

    override fun getInspectionById(id: Long): Flow<Inspection?> =
        dao.getInspectionById(id).map { it?.toDomain() }

    override suspend fun refreshByHive(hiveId: Long) {
        inspectionSource.getByHive(hiveId)
            .onSuccess { items ->
                dao.deleteByHive(hiveId)
                dao.insertAll(items.map { it.toEntity() })
            }
    }

    override suspend fun insertInspection(inspection: Inspection): Long {
        val server = inspectionSource.create(inspection.toCreateRequest()).getOrThrow()
        dao.insertInspection(server.toEntity())
        return server.id
    }

    override suspend fun updateInspection(inspection: Inspection) {
        inspectionSource.getById(inspection.id).getOrThrow()
        val server = inspectionSource.update(inspection.id, inspection.toUpdateRequest()).getOrThrow()
        dao.updateInspection(server.toEntity())
    }

    override suspend fun deleteInspection(id: Long) {
        inspectionSource.getById(id).getOrThrow()
        inspectionSource.delete(id).getOrThrow()
        dao.deleteInspection(id)
    }
}
