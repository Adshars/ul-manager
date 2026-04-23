package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Inspection
import kotlinx.coroutines.flow.Flow

interface InspectionRepository {
    fun getInspectionsByHive(hiveId: Long): Flow<List<Inspection>>
    fun getInspectionById(id: Long): Flow<Inspection?>
    suspend fun insertInspection(inspection: Inspection): Long
    suspend fun updateInspection(inspection: Inspection)
    suspend fun deleteInspection(id: Long)
}
