package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByApiary(apiaryId: Long): Flow<List<Task>>
    fun getTasksByHive(hiveId: Long): Flow<List<Task>>
    fun getTaskById(id: Long): Flow<Task?>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: Long)
    suspend fun setTaskCompleted(id: Long, completed: Boolean)
}
