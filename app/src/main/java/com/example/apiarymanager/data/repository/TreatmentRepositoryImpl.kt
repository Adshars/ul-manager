package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.TreatmentDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Treatment
import com.example.apiarymanager.domain.repository.TreatmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TreatmentRepositoryImpl @Inject constructor(
    private val dao: TreatmentDao
) : TreatmentRepository {

    override fun getTreatmentsByHive(hiveId: Long): Flow<List<Treatment>> =
        dao.getTreatmentsByHive(hiveId).map { it.map { e -> e.toDomain() } }

    override fun getTreatmentById(id: Long): Flow<Treatment?> =
        dao.getTreatmentById(id).map { it?.toDomain() }

    override suspend fun insertTreatment(treatment: Treatment): Long =
        dao.insertTreatment(treatment.toEntity())

    override suspend fun updateTreatment(treatment: Treatment) =
        dao.updateTreatment(treatment.toEntity())

    override suspend fun deleteTreatment(id: Long) =
        dao.deleteTreatment(id)
}
