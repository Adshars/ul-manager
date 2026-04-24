package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HiveRepositoryImpl @Inject constructor(
    private val dao: HiveDao
) : HiveRepository {

    override fun getHivesByApiary(apiaryId: Long): Flow<List<Hive>> =
        dao.getHivesByApiary(apiaryId).map { entities -> entities.map { it.toDomain() } }

    override fun getHiveById(id: Long): Flow<Hive?> =
        dao.getHiveById(id).map { it?.toDomain() }

    override suspend fun insertHive(hive: Hive): Long =
        dao.insertHive(hive.toEntity())

    override suspend fun updateHive(hive: Hive) =
        dao.updateHive(hive.toEntity())

    override suspend fun deleteHive(id: Long) =
        dao.deleteHive(id)

    override fun getActiveHiveCount(apiaryId: Long): Flow<Int> =
        dao.getActiveHiveCount(apiaryId)

    override suspend fun getHiveByQrCode(qrCode: String): Hive? =
        dao.getHiveByQrCode(qrCode)?.toDomain()
}
