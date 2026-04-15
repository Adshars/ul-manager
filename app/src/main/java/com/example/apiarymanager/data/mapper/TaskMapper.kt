package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.TaskDto
import com.example.apiarymanager.data.local.entity.TaskEntity
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.TaskPriority
import java.time.LocalDate

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun TaskDto.toEntity(): TaskEntity = TaskEntity(
    id          = id,
    apiaryId    = apiaryId,
    hiveId      = hiveId,
    title       = title,
    description = description,
    dueDate     = dueDate?.let { LocalDate.parse(it).toEpochDay() },
    priority    = priority,
    isCompleted = isCompleted,
    createdAt   = LocalDate.parse(createdAt).toEpochDay()
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun TaskEntity.toDomain(): Task = Task(
    id          = id,
    apiaryId    = apiaryId,
    hiveId      = hiveId,
    title       = title,
    description = description,
    dueDate     = dueDate?.let { LocalDate.ofEpochDay(it) },
    priority    = TaskPriority.valueOf(priority),
    isCompleted = isCompleted,
    createdAt   = LocalDate.ofEpochDay(createdAt)
)

// ─── Domain → Entity ─────────────────────────────────────────────────────────

fun Task.toEntity(): TaskEntity = TaskEntity(
    id          = id,
    apiaryId    = apiaryId,
    hiveId      = hiveId,
    title       = title,
    description = description,
    dueDate     = dueDate?.toEpochDay(),
    priority    = priority.name,
    isCompleted = isCompleted,
    createdAt   = createdAt.toEpochDay()
)
