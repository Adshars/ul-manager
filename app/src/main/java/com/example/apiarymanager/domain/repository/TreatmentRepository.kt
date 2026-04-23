package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Treatment
import kotlinx.coroutines.flow.Flow

interface TreatmentRepository {
    fun getTreatmentsByHive(hiveId: Long): Flow<List<Treatment>>
    fun getTreatmentById(id: Long): Flow<Treatment?>
    suspend fun insertTreatment(treatment: Treatment): Long
    suspend fun updateTreatment(treatment: Treatment)
    suspend fun deleteTreatment(id: Long)
}
