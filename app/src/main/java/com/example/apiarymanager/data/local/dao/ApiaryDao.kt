package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apiaries: List<ApiaryEntity>)

    @Update
    suspend fun updateApiary(apiary: ApiaryEntity)

    @Query("DELETE FROM apiaries WHERE id = :id")
    suspend fun deleteApiary(id: Long)

    @Query("SELECT COUNT(*) FROM apiaries")
    suspend fun count(): Int
}
