package com.example.apiarymanager.data.dto

/**
 * DTO representing the API contract for a Task.
 * [priority] mirrors enum: "LOW" | "MEDIUM" | "HIGH"
 */
data class TaskDto(
    val id: Long,
    val apiaryId: Long?,
    val hiveId: Long?,
    val title: String,
    val description: String,
    val dueDate: String?,       // ISO-8601 or null
    val priority: String,
    val isCompleted: Boolean,
    val createdAt: String       // ISO-8601
)
