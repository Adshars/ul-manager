package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apiarymanager.data.local.entity.HiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {

    @Query("SELECT * FROM hives WHERE apiary_id = :apiaryId ORDER BY number ASC")
    fun getHivesByApiary(apiaryId: Long): Flow<List<HiveEntity>>

    @Query("SELECT * FROM hives WHERE id = :id")
    fun getHiveById(id: Long): Flow<HiveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: HiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hives: List<HiveEntity>)

    @Update
    suspend fun updateHive(hive: HiveEntity)

    @Query("DELETE FROM hives WHERE id = :id")
    suspend fun deleteHive(id: Long)

    /** Active hives = ACTIVE + WEAK (not DEAD/SOLD). Used by Dashboard counters. */
    @Query("SELECT COUNT(*) FROM hives WHERE apiary_id = :apiaryId AND status IN ('ACTIVE', 'WEAK')")
    fun getActiveHiveCount(apiaryId: Long): Flow<Int>
}
