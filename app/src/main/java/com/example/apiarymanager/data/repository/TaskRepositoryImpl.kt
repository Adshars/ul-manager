package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> =
        dao.getAllTasks().map { entities -> entities.map { it.toDomain() } }

    override fun getTasksByApiary(apiaryId: Long): Flow<List<Task>> =
        dao.getTasksByApiary(apiaryId).map { entities -> entities.map { it.toDomain() } }

    override fun getTasksByHive(hiveId: Long): Flow<List<Task>> =
        dao.getTasksByHive(hiveId).map { entities -> entities.map { it.toDomain() } }

    override fun getTaskById(id: Long): Flow<Task?> =
        dao.getTaskById(id).map { it?.toDomain() }

    override suspend fun insertTask(task: Task): Long =
        dao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        dao.updateTask(task.toEntity())

    override suspend fun deleteTask(id: Long) =
        dao.deleteTask(id)

    override suspend fun setTaskCompleted(id: Long, completed: Boolean) =
        dao.setCompleted(id, completed)
}
