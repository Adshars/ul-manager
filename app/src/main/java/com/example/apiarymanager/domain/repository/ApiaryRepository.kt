package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Apiary
import kotlinx.coroutines.flow.Flow

interface ApiaryRepository {
    fun getAllApiaries(): Flow<List<Apiary>>
    fun getApiaryById(id: Long): Flow<Apiary?>
    suspend fun insertApiary(apiary: Apiary): Long
    suspend fun updateApiary(apiary: Apiary)
    suspend fun deleteApiary(id: Long)
}
