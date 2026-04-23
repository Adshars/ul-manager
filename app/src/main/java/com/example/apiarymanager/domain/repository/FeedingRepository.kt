package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Feeding
import kotlinx.coroutines.flow.Flow

interface FeedingRepository {
    fun getFeedingsByHive(hiveId: Long): Flow<List<Feeding>>
    fun getFeedingById(id: Long): Flow<Feeding?>
    suspend fun insertFeeding(feeding: Feeding): Long
    suspend fun updateFeeding(feeding: Feeding)
    suspend fun deleteFeeding(id: Long)
    fun getTotalFeedingKgByApiary(apiaryId: Long): Flow<Float>
}
