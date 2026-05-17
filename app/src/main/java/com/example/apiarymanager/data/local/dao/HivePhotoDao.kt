package com.example.apiarymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HivePhotoDao {

    @Query("SELECT * FROM hive_photos WHERE hive_id = :hiveId ORDER BY created_at DESC")
    fun getPhotosByHive(hiveId: Long): Flow<List<HivePhotoEntity>>

    @Query("SELECT * FROM hive_photos WHERE inspection_id = :inspectionId ORDER BY created_at DESC")
    suspend fun getPhotosByInspectionOnce(inspectionId: Long): List<HivePhotoEntity>

    @Query("SELECT * FROM hive_photos WHERE upload_status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPendingPhotos(): List<HivePhotoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhotos(photos: List<HivePhotoEntity>)

    @Query("UPDATE hive_photos SET upload_status = 'UPLOADED', remote_id = :remoteId WHERE id = :id")
    suspend fun markUploaded(id: Long, remoteId: Long)

    @Query("DELETE FROM hive_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)
}
