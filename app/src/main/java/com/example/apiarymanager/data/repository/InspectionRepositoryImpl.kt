package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InspectionRepositoryImpl @Inject constructor(
    private val dao: InspectionDao
) : InspectionRepository {

    override fun getInspectionsByHive(hiveId: Long): Flow<List<Inspection>> =
        dao.getInspectionsByHive(hiveId).map { entities -> entities.map { it.toDomain() } }

    override fun getInspectionById(id: Long): Flow<Inspection?> =
        dao.getInspectionById(id).map { it?.toDomain() }

    override suspend fun insertInspection(inspection: Inspection): Long =
        dao.insertInspection(inspection.toEntity())

    override suspend fun updateInspection(inspection: Inspection) =
        dao.updateInspection(inspection.toEntity())

    override suspend fun deleteInspection(id: Long) =
        dao.deleteInspection(id)
}
