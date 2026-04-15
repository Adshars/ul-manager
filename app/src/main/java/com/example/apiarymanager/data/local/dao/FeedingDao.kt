package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apiarymanager.data.local.entity.FeedingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {

    @Query("SELECT * FROM feedings WHERE hive_id = :hiveId ORDER BY date DESC")
    fun getFeedingsByHive(hiveId: Long): Flow<List<FeedingEntity>>

    @Query("SELECT * FROM feedings WHERE id = :id")
    fun getFeedingById(id: Long): Flow<FeedingEntity?>

    @Query("""
        SELECT COALESCE(SUM(f.weight_kg), 0)
        FROM feedings f
        INNER JOIN hives v ON v.id = f.hive_id
        WHERE v.apiary_id = :apiaryId
    """)
    fun getTotalFeedingKgByApiary(apiaryId: Long): Flow<Float>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeding(feeding: FeedingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feedings: List<FeedingEntity>)

    @Update
    suspend fun updateFeeding(feeding: FeedingEntity)

    @Query("DELETE FROM feedings WHERE id = :id")
    suspend fun deleteFeeding(id: Long)
}
