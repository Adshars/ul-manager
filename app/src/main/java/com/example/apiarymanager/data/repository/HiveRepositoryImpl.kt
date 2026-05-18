package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.mapper.toCreateRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.mapper.toUpdateRequest
import com.example.apiarymanager.data.remote.source.HiveSource
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HiveRepositoryImpl @Inject constructor(
    private val dao: HiveDao,
    private val hiveSource: HiveSource
) : HiveRepository {

    override fun getHivesByApiary(apiaryId: Long): Flow<List<Hive>> =
        dao.getHivesByApiary(apiaryId).map { entities -> entities.map { it.toDomain() } }

    override fun getHiveById(id: Long): Flow<Hive?> =
        dao.getHiveById(id).map { it?.toDomain() }

    override suspend fun refreshByApiary(apiaryId: Long) {
        hiveSource.getByApiary(apiaryId)
            .onSuccess { items ->
                val ids = items.map { it.id }
                if (ids.isEmpty()) dao.deleteByApiary(apiaryId) else dao.deleteNotIn(apiaryId, ids)
                dao.insertAll(items.map { it.toEntity() })
            }
    }

    override suspend fun insertHive(hive: Hive): Long {
        val server = hiveSource.create(hive.toCreateRequest()).getOrThrow()
        dao.insertHive(server.toEntity())
        return server.id
    }

    override suspend fun updateHive(hive: Hive) {
        hiveSource.getById(hive.id).getOrThrow()
        val server = hiveSource.update(hive.id, hive.toUpdateRequest()).getOrThrow()
        dao.updateHive(server.toEntity())
    }

    override suspend fun deleteHive(id: Long) {
        hiveSource.getById(id).getOrThrow()
        hiveSource.delete(id).getOrThrow()
        dao.deleteHive(id)
    }

    override fun getActiveHiveCount(apiaryId: Long): Flow<Int> =
        dao.getActiveHiveCount(apiaryId)

    override suspend fun getHiveByQrCode(qrCode: String): Hive? =
        dao.getHiveByQrCode(qrCode)?.toDomain()

    override fun getAllHives(): Flow<List<Hive>> =
        dao.getAllHives().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getDistinctQueenYears(): List<Int> =
        dao.getDistinctQueenYears()
}
