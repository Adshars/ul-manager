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

    // ── Observations ──────────────────────────────────────────────────────────
    @ColumnInfo(name = "queen_seen")
    val queenSeen: Boolean,

    @ColumnInfo(name = "brood_seen")
    val broodSeen: Boolean,

    @ColumnInfo(name = "queen_cells_seen")
    val queenCellsSeen: Boolean,

    // ── Colony condition ──────────────────────────────────────────────────────
    /** Stored as ColonyStrength enum name string */
    @ColumnInfo(name = "colony_strength")
    val colonyStrength: String,

    /** Stored as BroodCondition enum name string */
    @ColumnInfo(name = "brood_condition")
    val broodCondition: String,

    @ColumnInfo(name = "honey_stores_kg")
    val honeyStoresKg: Float,

    // ── Frame management ──────────────────────────────────────────────────────
    @ColumnInfo(name = "frame_count")
    val frameCount: Int,

    @ColumnInfo(name = "superboxes_added")
    val superboxesAdded: Int,

    @ColumnInfo(name = "superboxes_removed")
    val superboxesRemoved: Int,

    @ColumnInfo(name = "dry_comb_frames")
    val dryCombFrames: Int,

    @ColumnInfo(name = "foundation_frames")
    val foundationFrames: Int,

    // ── Free text ─────────────────────────────────────────────────────────────
    @ColumnInfo(name = "problems")
    val problems: String,

    @ColumnInfo(name = "notes")
    val notes: String
)
