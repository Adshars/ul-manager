package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apiarymanager.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {

    @Query("SELECT * FROM treatments WHERE hive_id = :hiveId ORDER BY date DESC")
    fun getTreatmentsByHive(hiveId: Long): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM treatments WHERE id = :id")
    fun getTreatmentById(id: Long): Flow<TreatmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(treatment: TreatmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(treatments: List<TreatmentEntity>)

    @Update
    suspend fun updateTreatment(treatment: TreatmentEntity)

    @Query("DELETE FROM treatments WHERE id = :id")
    suspend fun deleteTreatment(id: Long)
}
