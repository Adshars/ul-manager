package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apiarymanager.data.local.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {

    @Query("SELECT * FROM inspections WHERE hive_id = :hiveId ORDER BY date DESC")
    fun getInspectionsByHive(hiveId: Long): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id")
    fun getInspectionById(id: Long): Flow<InspectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspections: List<InspectionEntity>)

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspection(id: Long)
}
