package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Hive
import kotlinx.coroutines.flow.Flow

interface HiveRepository {
    fun getHivesByApiary(apiaryId: Long): Flow<List<Hive>>
    fun getHiveById(id: Long): Flow<Hive?>
    suspend fun insertHive(hive: Hive): Long
    suspend fun updateHive(hive: Hive)
    suspend fun deleteHive(id: Long)
    fun getActiveHiveCount(apiaryId: Long): Flow<Int>
}
