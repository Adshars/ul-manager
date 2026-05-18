package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.TreatmentDao
import com.example.apiarymanager.data.mapper.toCreateRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.mapper.toUpdateRequest
import com.example.apiarymanager.data.remote.source.TreatmentSource
import com.example.apiarymanager.domain.model.Treatment
import com.example.apiarymanager.domain.repository.TreatmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TreatmentRepositoryImpl @Inject constructor(
    private val dao: TreatmentDao,
    private val treatmentSource: TreatmentSource
) : TreatmentRepository {

    override fun getTreatmentsByHive(hiveId: Long): Flow<List<Treatment>> =
        dao.getTreatmentsByHive(hiveId).map { it.map { e -> e.toDomain() } }

    override fun getTreatmentById(id: Long): Flow<Treatment?> =
        dao.getTreatmentById(id).map { it?.toDomain() }

    override suspend fun refreshByHive(hiveId: Long) {
        treatmentSource.getByHive(hiveId)
            .onSuccess { items ->
                dao.deleteByHive(hiveId)
                dao.insertAll(items.map { it.toEntity() })
            }
    }

    override suspend fun insertTreatment(treatment: Treatment): Long {
        val server = treatmentSource.create(treatment.toCreateRequest()).getOrThrow()
        dao.insertTreatment(server.toEntity())
        return server.id
    }

    override suspend fun updateTreatment(treatment: Treatment) {
        treatmentSource.getById(treatment.id).getOrThrow()
        val server = treatmentSource.update(treatment.id, treatment.toUpdateRequest()).getOrThrow()
        dao.updateTreatment(server.toEntity())
    }

    override suspend fun deleteTreatment(id: Long) {
        treatmentSource.getById(id).getOrThrow()
        treatmentSource.delete(id).getOrThrow()
        dao.deleteTreatment(id)
    }
}
