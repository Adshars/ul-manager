package com.example.apiarymanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.local.dao.FeedingDao
import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.local.dao.HoneyHarvestDao
import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.local.dao.TreatmentDao
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import com.example.apiarymanager.data.local.entity.FeedingEntity
import com.example.apiarymanager.data.local.entity.HiveEntity
import com.example.apiarymanager.data.local.entity.HoneyHarvestEntity
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.data.local.entity.TaskEntity
import com.example.apiarymanager.data.local.entity.TreatmentEntity

@Database(
    entities = [
        ApiaryEntity::class,
        HiveEntity::class,
        InspectionEntity::class,
        TaskEntity::class,
        HoneyHarvestEntity::class,
        TreatmentEntity::class,
        FeedingEntity::class
    ],
    version = 4,   // v4: added qr_code column to hives
    exportSchema = false
)
abstract class ApiaryManagerDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun taskDao(): TaskDao
    abstract fun honeyHarvestDao(): HoneyHarvestDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun feedingDao(): FeedingDao

    companion object {
        const val DATABASE_NAME = "apiary_manager.db"
    }
}
