package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

/** [priority] mirrors enum: "LOW" | "MEDIUM" | "HIGH" */
@Serializable
data class TaskDto(
    val id: Long,
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val title: String,
    val description: String = "",
    val dueDate: String? = null,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val emailNotificationEnabled: Boolean = false,
    val notificationAt: String? = null
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val dueDate: String? = null,
    val emailNotificationEnabled: Boolean = false,
    val notificationAt: String? = null
)

@Serializable
data class UpdateTaskRequest(
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val dueDate: String? = null,
    val emailNotificationEnabled: Boolean = false,
    val notificationAt: String? = null
)

@Serializable
data class CompleteTaskRequest(
    val completed: Boolean
)
