package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.HoneyHarvest
import kotlinx.coroutines.flow.Flow

interface HoneyHarvestRepository {
    fun getHarvestsByHive(hiveId: Long): Flow<List<HoneyHarvest>>
    fun getHarvestById(id: Long): Flow<HoneyHarvest?>
    suspend fun insertHarvest(harvest: HoneyHarvest): Long
    suspend fun updateHarvest(harvest: HoneyHarvest)
    suspend fun deleteHarvest(id: Long)
    fun getTotalHarvestKgByApiary(apiaryId: Long): Flow<Float>
}
