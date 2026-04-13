package com.example.apiarymanager.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.apiarymanager.data.local.database.ApiaryManagerDatabase
import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.local.seeder.DatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope appScope: CoroutineScope
    ): ApiaryManagerDatabase {
        // lateinit trick: the callback fires on first DB access (not at build() time),
        // so `database` is guaranteed to be assigned before the lambda runs.
        lateinit var database: ApiaryManagerDatabase

        database = Room.databaseBuilder(
            context,
            ApiaryManagerDatabase::class.java,
            ApiaryManagerDatabase.DATABASE_NAME
        )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    appScope.launch {
                        DatabaseSeeder.seed(database)
                    }
                }
            })
            .build()

        return database
    }

    @Provides
    fun provideApiaryDao(db: ApiaryManagerDatabase): ApiaryDao = db.apiaryDao()

    @Provides
    fun provideHiveDao(db: ApiaryManagerDatabase): HiveDao = db.hiveDao()

    @Provides
    fun provideInspectionDao(db: ApiaryManagerDatabase): InspectionDao = db.inspectionDao()

    @Provides
    fun provideTaskDao(db: ApiaryManagerDatabase): TaskDao = db.taskDao()
}
