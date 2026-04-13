package com.example.apiarymanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import com.example.apiarymanager.data.local.entity.HiveEntity
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.data.local.entity.TaskEntity

@Database(
    entities = [
        ApiaryEntity::class,
        HiveEntity::class,
        InspectionEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApiaryManagerDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "apiary_manager.db"
    }
}
