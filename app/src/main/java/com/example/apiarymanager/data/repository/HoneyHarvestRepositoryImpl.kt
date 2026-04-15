package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.HoneyHarvestDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HoneyHarvestRepositoryImpl @Inject constructor(
    private val dao: HoneyHarvestDao
) : HoneyHarvestRepository {

    override fun getHarvestsByHive(hiveId: Long): Flow<List<HoneyHarvest>> =
        dao.getHarvestsByHive(hiveId).map { it.map { e -> e.toDomain() } }

    override fun getHarvestById(id: Long): Flow<HoneyHarvest?> =
        dao.getHarvestById(id).map { it?.toDomain() }

    override suspend fun insertHarvest(harvest: HoneyHarvest): Long =
        dao.insertHarvest(harvest.toEntity())

    override suspend fun updateHarvest(harvest: HoneyHarvest) =
        dao.updateHarvest(harvest.toEntity())

    override suspend fun deleteHarvest(id: Long) =
        dao.deleteHarvest(id)

    override fun getTotalHarvestKgByApiary(apiaryId: Long): Flow<Float> =
        dao.getTotalHarvestKgByApiary(apiaryId)
}
