package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {

    @Query("SELECT * FROM apiaries ORDER BY name ASC")
    fun getAllApiaries(): Flow<List<ApiaryEntity>>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    fun getApiaryById(id: Long): Flow<ApiaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiary(apiary: ApiaryEntity): Long

    @Upsert
    suspend fun insertAll(apiaries: List<ApiaryEntity>)

    @Update
    suspend fun updateApiary(apiary: ApiaryEntity)

    @Query("DELETE FROM apiaries WHERE id = :id")
    suspend fun deleteApiary(id: Long)

    @Query("DELETE FROM apiaries")
    suspend fun deleteAll()

    @Query("DELETE FROM apiaries WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM apiaries")
    suspend fun count(): Int
}
