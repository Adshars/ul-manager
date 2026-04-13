package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.InspectionDto
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.domain.model.BroodCondition
import com.example.apiarymanager.domain.model.Inspection
import java.time.LocalDate

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun InspectionDto.toEntity(): InspectionEntity = InspectionEntity(
    id             = id,
    hiveId         = hiveId,
    date           = LocalDate.parse(date).toEpochDay(),
    queenSeen      = queenSeen,
    broodCondition = broodCondition,
    honeyStoresKg  = honeyStoresKg,
    frameCount     = frameCount,
    notes          = notes
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun InspectionEntity.toDomain(): Inspection = Inspection(
    id             = id,
    hiveId         = hiveId,
    date           = LocalDate.ofEpochDay(date),
    queenSeen      = queenSeen,
    broodCondition = BroodCondition.valueOf(broodCondition),
    honeyStoresKg  = honeyStoresKg,
    frameCount     = frameCount,
    notes          = notes
)

// ─── Domain → Entity ─────────────────────────────────────────────────────────

fun Inspection.toEntity(): InspectionEntity = InspectionEntity(
    id             = id,
    hiveId         = hiveId,
    date           = date.toEpochDay(),
    queenSeen      = queenSeen,
    broodCondition = broodCondition.name,
    honeyStoresKg  = honeyStoresKg,
    frameCount     = frameCount,
    notes          = notes
)
