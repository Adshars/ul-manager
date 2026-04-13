package com.example.apiarymanager.di

import com.example.apiarymanager.data.repository.ApiaryRepositoryImpl
import com.example.apiarymanager.data.repository.HiveRepositoryImpl
import com.example.apiarymanager.data.repository.InspectionRepositoryImpl
import com.example.apiarymanager.data.repository.TaskRepositoryImpl
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.InspectionRepository
import com.example.apiarymanager.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindApiaryRepository(impl: ApiaryRepositoryImpl): ApiaryRepository

    @Binds
    @Singleton
    abstract fun bindHiveRepository(impl: HiveRepositoryImpl): HiveRepository

    @Binds
    @Singleton
    abstract fun bindInspectionRepository(impl: InspectionRepositoryImpl): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
