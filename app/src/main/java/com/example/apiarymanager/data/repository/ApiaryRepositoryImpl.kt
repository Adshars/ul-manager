package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.remote.source.ApiarySource
import com.example.apiarymanager.di.ApplicationScope
import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.repository.ApiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiaryRepositoryImpl @Inject constructor(
    private val dao: ApiaryDao,
    private val apiarySource: ApiarySource,
    @ApplicationScope private val appScope: CoroutineScope
) : ApiaryRepository {

    init {
        appScope.launch { refresh() }
    }

    override fun getAllApiaries(): Flow<List<Apiary>> =
        dao.getAllApiaries().map { entities -> entities.map { it.toDomain() } }

    override fun getApiaryById(id: Long): Flow<Apiary?> =
        dao.getApiaryById(id).map { it?.toDomain() }

    override suspend fun refresh() {
        apiarySource.getAll()
            .onSuccess { items -> dao.insertAll(items.map { it.toEntity() }) }
    }

    override suspend fun insertApiary(apiary: Apiary): Long =
        dao.insertApiary(apiary.toEntity())

    override suspend fun updateApiary(apiary: Apiary) =
        dao.updateApiary(apiary.toEntity())

    override suspend fun deleteApiary(id: Long) =
        dao.deleteApiary(id)
}
