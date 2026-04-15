package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feedings",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hive_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hive_id")]
)
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hive_id")
    val hiveId: Long,

    /** Stored as epoch day */
    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "food_type")
    val foodType: String,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Float,

    @ColumnInfo(name = "application_method")
    val applicationMethod: String
)
