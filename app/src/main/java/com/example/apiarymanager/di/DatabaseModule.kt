package com.example.apiarymanager.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.apiarymanager.data.local.database.ApiaryManagerDatabase
import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.local.dao.FeedingDao
import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.local.dao.HivePhotoDao
import com.example.apiarymanager.data.local.dao.HoneyHarvestDao
import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.local.dao.TreatmentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ApiaryManagerDatabase =
        Room.databaseBuilder(
            context,
            ApiaryManagerDatabase::class.java,
            ApiaryManagerDatabase.DATABASE_NAME
        )
            .addMigrations(
                ApiaryManagerDatabase.MIGRATION_4_5,
                ApiaryManagerDatabase.MIGRATION_5_6,
                ApiaryManagerDatabase.MIGRATION_6_7,
                ApiaryManagerDatabase.MIGRATION_7_8,
                ApiaryManagerDatabase.MIGRATION_8_9,
                ApiaryManagerDatabase.MIGRATION_9_10
            )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            })
            .build()

    @Provides
    fun provideApiaryDao(db: ApiaryManagerDatabase): ApiaryDao = db.apiaryDao()

    @Provides
    fun provideHiveDao(db: ApiaryManagerDatabase): HiveDao = db.hiveDao()

    @Provides
    fun provideInspectionDao(db: ApiaryManagerDatabase): InspectionDao = db.inspectionDao()

    @Provides
    fun provideTaskDao(db: ApiaryManagerDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideHoneyHarvestDao(db: ApiaryManagerDatabase): HoneyHarvestDao = db.honeyHarvestDao()

    @Provides
    fun provideTreatmentDao(db: ApiaryManagerDatabase): TreatmentDao = db.treatmentDao()

    @Provides
    fun provideFeedingDao(db: ApiaryManagerDatabase): FeedingDao = db.feedingDao()

    @Provides
    fun provideHivePhotoDao(db: ApiaryManagerDatabase): HivePhotoDao = db.hivePhotoDao()
}
