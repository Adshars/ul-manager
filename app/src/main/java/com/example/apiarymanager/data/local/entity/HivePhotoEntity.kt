package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hive_photos",
    foreignKeys = [
        ForeignKey(
            entity        = HiveEntity::class,
            parentColumns = ["id"],
            childColumns  = ["hive_id"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity        = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns  = ["inspection_id"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("hive_id"), Index("inspection_id")]
)
data class HivePhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "hive_id")       val hiveId: Long,
    @ColumnInfo(name = "inspection_id") val inspectionId: Long?,
    @ColumnInfo(name = "local_path")    val localPath: String,
    @ColumnInfo(name = "created_at")    val createdAt: String
)
