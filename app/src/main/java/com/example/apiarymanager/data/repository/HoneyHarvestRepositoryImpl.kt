package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.HoneyHarvestDao
import com.example.apiarymanager.data.mapper.toCreateRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.mapper.toUpdateRequest
import com.example.apiarymanager.data.remote.source.HoneyHarvestSource
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HoneyHarvestRepositoryImpl @Inject constructor(
    private val dao: HoneyHarvestDao,
    private val honeyHarvestSource: HoneyHarvestSource
) : HoneyHarvestRepository {

    override fun getHarvestsByHive(hiveId: Long): Flow<List<HoneyHarvest>> =
        dao.getHarvestsByHive(hiveId).map { it.map { e -> e.toDomain() } }

    override fun getHarvestById(id: Long): Flow<HoneyHarvest?> =
        dao.getHarvestById(id).map { it?.toDomain() }

    override suspend fun refreshByHive(hiveId: Long) {
        honeyHarvestSource.getByHive(hiveId)
            .onSuccess { items ->
                dao.deleteByHive(hiveId)
                dao.insertAll(items.map { it.toEntity() })
            }
    }

    override suspend fun insertHarvest(harvest: HoneyHarvest): Long {
        val server = honeyHarvestSource.create(harvest.toCreateRequest()).getOrThrow()
        dao.insertHarvest(server.toEntity())
        return server.id
    }

    override suspend fun updateHarvest(harvest: HoneyHarvest) {
        honeyHarvestSource.getById(harvest.id).getOrThrow()
        val server = honeyHarvestSource.update(harvest.id, harvest.toUpdateRequest()).getOrThrow()
        dao.updateHarvest(server.toEntity())
    }

    override suspend fun deleteHarvest(id: Long) {
        honeyHarvestSource.getById(id).getOrThrow()
        honeyHarvestSource.delete(id).getOrThrow()
        dao.deleteHarvest(id)
    }

    override fun getTotalHarvestKgByApiary(apiaryId: Long): Flow<Float> =
        dao.getTotalHarvestKgByApiary(apiaryId)
}
