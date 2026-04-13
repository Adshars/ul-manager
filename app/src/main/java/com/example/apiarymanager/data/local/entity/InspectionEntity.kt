package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspections",
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
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hive_id")
    val hiveId: Long,

    /** Stored as epoch day */
    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "queen_seen")
    val queenSeen: Boolean,

    /** Stored as enum name string */
    @ColumnInfo(name = "brood_condition")
    val broodCondition: String,

    @ColumnInfo(name = "honey_stores_kg")
    val honeyStoresKg: Float,

    @ColumnInfo(name = "frame_count")
    val frameCount: Int,

    @ColumnInfo(name = "notes")
    val notes: String
)
