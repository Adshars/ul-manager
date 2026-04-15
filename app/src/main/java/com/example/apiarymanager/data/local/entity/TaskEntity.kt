package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiary_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hive_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
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
    val createdAt: Long
)
