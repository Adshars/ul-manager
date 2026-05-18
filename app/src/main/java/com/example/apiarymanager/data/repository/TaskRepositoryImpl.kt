package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.mapper.toCreateRequest
import com.example.apiarymanager.data.mapper.toDomain
import com.example.apiarymanager.data.mapper.toEntity
import com.example.apiarymanager.data.mapper.toUpdateRequest
import com.example.apiarymanager.data.remote.source.TaskSource
import com.example.apiarymanager.di.ApplicationScope
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val taskSource: TaskSource,
    @ApplicationScope private val appScope: CoroutineScope
) : TaskRepository {

    init {
        appScope.launch { refresh() }
    }

    override fun getAllTasks(): Flow<List<Task>> =
        dao.getAllTasks().map { entities -> entities.map { it.toDomain() } }

    override fun getTasksByApiary(apiaryId: Long): Flow<List<Task>> =
        dao.getTasksByApiary(apiaryId).map { entities -> entities.map { it.toDomain() } }

    override fun getTasksByHive(hiveId: Long): Flow<List<Task>> =
        dao.getTasksByHive(hiveId).map { entities -> entities.map { it.toDomain() } }

    override fun getTaskById(id: Long): Flow<Task?> =
        dao.getTaskById(id).map { it?.toDomain() }

    override suspend fun refresh() {
        taskSource.getAll()
            .onSuccess { items ->
                dao.deleteAll()
                dao.insertAll(items.map { it.toEntity() })
            }
    }

    override suspend fun insertTask(task: Task): Long {
        val server = taskSource.create(task.toCreateRequest()).getOrThrow()
        dao.insertTask(server.toEntity())
        return server.id
    }

    override suspend fun updateTask(task: Task) {
        taskSource.getById(task.id).getOrThrow()
        val server = taskSource.update(task.id, task.toUpdateRequest()).getOrThrow()
        dao.updateTask(server.toEntity())
    }

    override suspend fun deleteTask(id: Long) {
        taskSource.getById(id).getOrThrow()
        taskSource.delete(id).getOrThrow()
        dao.deleteTask(id)
    }

    override suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        taskSource.getById(id).getOrThrow()
        val server = taskSource.complete(id, completed).getOrThrow()
        dao.updateTask(server.toEntity())
    }
}
