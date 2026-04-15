package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.FeedingDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.repository.FeedingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedingRepositoryImpl @Inject constructor(
    private val dao: FeedingDao
) : FeedingRepository {

    override fun getFeedingsByHive(hiveId: Long): Flow<List<Feeding>> =
        dao.getFeedingsByHive(hiveId).map { it.map { e -> e.toDomain() } }

    override fun getFeedingById(id: Long): Flow<Feeding?> =
        dao.getFeedingById(id).map { it?.toDomain() }

    override suspend fun insertFeeding(feeding: Feeding): Long =
        dao.insertFeeding(feeding.toEntity())

    override suspend fun updateFeeding(feeding: Feeding) =
        dao.updateFeeding(feeding.toEntity())

    override suspend fun deleteFeeding(id: Long) =
        dao.deleteFeeding(id)

    override fun getTotalFeedingKgByApiary(apiaryId: Long): Flow<Float> =
        dao.getTotalFeedingKgByApiary(apiaryId)
}
