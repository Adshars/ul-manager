package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.core.util.parseEnum
import com.example.apiarymanager.data.dto.CreateInspectionRequest
import com.example.apiarymanager.data.dto.InspectionDto
import com.example.apiarymanager.data.dto.UpdateInspectionRequest
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.domain.model.BroodCondition
import com.example.apiarymanager.domain.model.ColonyStrength
import com.example.apiarymanager.domain.model.Inspection
import java.time.LocalDate

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun InspectionDto.toDomain(): Inspection = Inspection(
    id               = id,
    hiveId           = hiveId,
    date             = LocalDate.parse(date),
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    broodCondition   = parseEnum(broodCondition, BroodCondition.GOOD),
    colonyStrength   = parseEnum(colonyStrength, ColonyStrength.NORMAL),
    honeyStoresKg    = honeyStoresKg,
    frameCount       = frameCount,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes,
    photoPaths       = emptyList(),
    newQueenYear     = newQueenYear
)

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
    notes            = notes,
    newQueenYear     = newQueenYear
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun InspectionEntity.toDomain(): Inspection = Inspection(
    id               = id,
    hiveId           = hiveId,
    date             = LocalDate.ofEpochDay(date),
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    broodCondition   = parseEnum(broodCondition, BroodCondition.GOOD),
    colonyStrength   = parseEnum(colonyStrength, ColonyStrength.NORMAL),
    honeyStoresKg    = honeyStoresKg,
    frameCount       = frameCount,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes,
    photoPaths       = if (photoPaths.isBlank()) emptyList() else photoPaths.split(","),
    newQueenYear     = newQueenYear
)

// ─── Domain → Entity ─────────────────────────────────────────────────────────

fun Inspection.toCreateRequest() = CreateInspectionRequest(
    hiveId            = hiveId,
    date              = date.toString(),
    queenSeen         = queenSeen,
    broodSeen         = broodSeen,
    queenCellsSeen    = queenCellsSeen,
    colonyStrength    = colonyStrength.ordinal,
    broodCondition    = broodCondition.ordinal,
    honeyStoresKg     = honeyStoresKg,
    frameCount        = frameCount,
    superboxesAdded   = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames     = dryCombFrames,
    foundationFrames  = foundationFrames,
    problems          = problems.takeIf { it.isNotBlank() },
    notes             = notes.takeIf { it.isNotBlank() },
    newQueenYear      = newQueenYear
)

fun Inspection.toUpdateRequest() = UpdateInspectionRequest(
    date              = date.toString(),
    queenSeen         = queenSeen,
    broodSeen         = broodSeen,
    queenCellsSeen    = queenCellsSeen,
    colonyStrength    = colonyStrength.ordinal,
    broodCondition    = broodCondition.ordinal,
    honeyStoresKg     = honeyStoresKg,
    frameCount        = frameCount,
    superboxesAdded   = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames     = dryCombFrames,
    foundationFrames  = foundationFrames,
    problems          = problems.takeIf { it.isNotBlank() },
    notes             = notes.takeIf { it.isNotBlank() },
    newQueenYear      = newQueenYear
)

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
    notes            = notes,
    photoPaths       = photoPaths.joinToString(","),
    newQueenYear     = newQueenYear
)
