package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hives",
    foreignKeys = [
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiary_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("apiary_id")]
)
data class HiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "apiary_id")
    val apiaryId: Long,

    @ColumnInfo(name = "number")
    val number: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "queen_year")
    val queenYear: Int?,

    /** Stored as enum name string */
    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "notes")
    val notes: String,

    /** Stored as epoch day */
    @ColumnInfo(name = "installed_at")
    val installedAt: Long
)
