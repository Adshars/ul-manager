package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index("apiary_id"), Index("hive_id")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "apiary_id")
    val apiaryId: Long?,

    @ColumnInfo(name = "hive_id")
    val hiveId: Long?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    /** Stored as epoch day, nullable */
    @ColumnInfo(name = "due_date")
    val dueDate: Long?,

    /** Stored as enum name string */
    @ColumnInfo(name = "priority")
    val priority: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    /** Stored as epoch day */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "email_notification_enabled")
    val emailNotificationEnabled: Boolean = false,

    /** Stored as ISO-8601 string, e.g. "2025-06-01T09:00:00", nullable */
    @ColumnInfo(name = "notification_at")
    val notificationAt: String? = null
)
