package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.repository.ApiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ApiaryRepositoryImpl @Inject constructor(
    private val dao: ApiaryDao
) : ApiaryRepository {

    override fun getAllApiaries(): Flow<List<Apiary>> =
        dao.getAllApiaries().map { entities -> entities.map { it.toDomain() } }

    override fun getApiaryById(id: Long): Flow<Apiary?> =
        dao.getApiaryById(id).map { it?.toDomain() }

    override suspend fun insertApiary(apiary: Apiary): Long =
        dao.insertApiary(apiary.toEntity())

    override suspend fun updateApiary(apiary: Apiary) =
        dao.updateApiary(apiary.toEntity())

    override suspend fun deleteApiary(id: Long) =
        dao.deleteApiary(id)
}
