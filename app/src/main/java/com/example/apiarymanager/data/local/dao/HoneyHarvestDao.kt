package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apiarymanager.data.local.entity.HoneyHarvestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoneyHarvestDao {

    @Query("SELECT * FROM honey_harvests WHERE hive_id = :hiveId ORDER BY date DESC")
    fun getHarvestsByHive(hiveId: Long): Flow<List<HoneyHarvestEntity>>

    @Query("SELECT * FROM honey_harvests WHERE id = :id")
    fun getHarvestById(id: Long): Flow<HoneyHarvestEntity?>

    @Query("""
        SELECT COALESCE(SUM(h.weight_kg), 0)
        FROM honey_harvests h
        INNER JOIN hives v ON v.id = h.hive_id
        WHERE v.apiary_id = :apiaryId
    """)
    fun getTotalHarvestKgByApiary(apiaryId: Long): Flow<Float>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: HoneyHarvestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(harvests: List<HoneyHarvestEntity>)

    @Update
    suspend fun updateHarvest(harvest: HoneyHarvestEntity)

    @Query("DELETE FROM honey_harvests WHERE id = :id")
    suspend fun deleteHarvest(id: Long)
}
