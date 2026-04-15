package com.example.apiarymanager.domain.model

import java.time.LocalDate

data class Task(
    val id: Long = 0,
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: LocalDate = LocalDate.now()
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH;

    fun displayName(): String = when (this) {
        LOW    -> "Niski"
        MEDIUM -> "Średni"
        HIGH   -> "Wysoki"
    }
}
