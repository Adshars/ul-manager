package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.InspectionDto
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.domain.model.BroodCondition
import com.example.apiarymanager.domain.model.ColonyStrength
import com.example.apiarymanager.domain.model.Inspection
import java.time.LocalDate

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun InspectionDto.toEntity(): InspectionEntity = InspectionEntity(
    id               = id,
    hiveId           = hiveId,
    date             = LocalDate.parse(date).toEpochDay(),
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    broodCondition   = broodCondition,
    colonyStrength   = colonyStrength,
    honeyStoresKg    = honeyStoresKg,
    frameCount       = frameCount,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun InspectionEntity.toDomain(): Inspection = Inspection(
    id               = id,
    hiveId           = hiveId,
    date             = LocalDate.ofEpochDay(date),
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    broodCondition   = BroodCondition.valueOf(broodCondition),
    colonyStrength   = ColonyStrength.valueOf(colonyStrength),
    honeyStoresKg    = honeyStoresKg,
    frameCount       = frameCount,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes
)

// ─── Domain → Entity ─────────────────────────────────────────────────────────

fun Inspection.toEntity(): InspectionEntity = InspectionEntity(
    id               = id,
    hiveId           = hiveId,
    date             = date.toEpochDay(),
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    broodCondition   = broodCondition.name,
    colonyStrength   = colonyStrength.name,
    honeyStoresKg    = honeyStoresKg,
    frameCount       = frameCount,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes
)
