package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.core.util.parseEnum
import com.example.apiarymanager.data.dto.CreateTaskRequest
import com.example.apiarymanager.data.dto.TaskDto
import com.example.apiarymanager.data.dto.UpdateTaskRequest
import com.example.apiarymanager.data.local.entity.TaskEntity
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalDateTime

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun TaskDto.toDomain(): Task = Task(
    id          = id,
    apiaryId    = apiaryId,
    hiveId      = hiveId,
    title       = title,
    description = description,
    dueDate     = dueDate?.let { LocalDate.parse(it) },
    priority    = parseEnum(priority, TaskPriority.MEDIUM),
    isCompleted = isCompleted,
    createdAt   = createdAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) } ?: LocalDate.now(),
    emailNotificationEnabled = emailNotificationEnabled,
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) }
)

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
    createdAt   = createdAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it).toEpochDay() } ?: LocalDate.now().toEpochDay()
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun TaskEntity.toDomain(): Task = Task(
    id          = id,
    apiaryId    = apiaryId,
    hiveId      = hiveId,
    title       = title,
    description = description,
    dueDate     = dueDate?.let { LocalDate.ofEpochDay(it) },
    priority    = parseEnum(priority, TaskPriority.MEDIUM),
    isCompleted = isCompleted,
    createdAt   = LocalDate.ofEpochDay(createdAt),
    emailNotificationEnabled = emailNotificationEnabled,
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) }
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
    createdAt   = createdAt.toEpochDay(),
    emailNotificationEnabled = emailNotificationEnabled,
    notificationAt = notificationAt?.toString()
)

fun Task.toCreateRequest() = CreateTaskRequest(
    title                    = title,
    description              = description,
    priority                 = priority.ordinal,
    apiaryId                 = apiaryId,
    hiveId                   = hiveId,
    dueDate                  = dueDate?.toString(),
    emailNotificationEnabled = emailNotificationEnabled,
    notificationAt           = notificationAt?.toString()
)

fun Task.toUpdateRequest() = UpdateTaskRequest(
    title                    = title,
    description              = description,
    priority                 = priority.ordinal,
    apiaryId                 = apiaryId,
    hiveId                   = hiveId,
    dueDate                  = dueDate?.toString(),
    emailNotificationEnabled = emailNotificationEnabled,
    notificationAt           = notificationAt?.toString()
)
