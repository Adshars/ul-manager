package com.example.apiarymanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatments",
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
data class TreatmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hive_id")
    val hiveId: Long,

    /** Stored as epoch day */
    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "medicine_type")
    val medicineType: String,

    @ColumnInfo(name = "dosage")
    val dosage: String,

    @ColumnInfo(name = "application_method")
    val applicationMethod: String,

    @ColumnInfo(name = "mortality_after_treatment")
    val mortalityAfterTreatment: String
)
