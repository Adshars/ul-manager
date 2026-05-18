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

    @Query("DELETE FROM hives WHERE apiary_id = :apiaryId")
    suspend fun deleteByApiary(apiaryId: Long)

    @Query("DELETE FROM hives WHERE apiary_id = :apiaryId AND id NOT IN (:ids)")
    suspend fun deleteNotIn(apiaryId: Long, ids: List<Long>)

    /** Active hives = ACTIVE + WEAK (not DEAD). Used by Dashboard counters. */
    @Query("SELECT COUNT(*) FROM hives WHERE apiary_id = :apiaryId AND status IN ('ACTIVE', 'WEAK')")
    fun getActiveHiveCount(apiaryId: Long): Flow<Int>

    @Query("SELECT * FROM hives WHERE qr_code = :qrCode LIMIT 1")
    suspend fun getHiveByQrCode(qrCode: String): HiveEntity?

    @Query("SELECT * FROM hives ORDER BY apiary_id ASC, number ASC")
    fun getAllHives(): Flow<List<HiveEntity>>

    @Query("SELECT DISTINCT queen_year FROM hives WHERE queen_year IS NOT NULL ORDER BY queen_year DESC")
    suspend fun getDistinctQueenYears(): List<Int>
}
