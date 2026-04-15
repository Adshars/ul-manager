package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.HiveDto
import com.example.apiarymanager.data.local.entity.HiveEntity
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.HiveStatus
import java.time.LocalDate

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun HiveDto.toEntity(): HiveEntity = HiveEntity(
    id          = id,
    apiaryId    = apiaryId,
    number      = number,
    name        = name,
    queenYear   = queenYear,
    status      = status,
    notes       = notes,
    installedAt = LocalDate.parse(installedAt).toEpochDay()
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun HiveEntity.toDomain(): Hive = Hive(
    id            = id,
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    queenYear     = queenYear,
    status        = HiveStatus.valueOf(status),
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin,
    notes         = notes,
    installedAt   = LocalDate.ofEpochDay(installedAt)
)

// ─── Domain → Entity ─────────────────────────────────────────────────────────

fun Hive.toEntity(): HiveEntity = HiveEntity(
    id            = id,
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    queenYear     = queenYear,
    status        = status.name,
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin,
    notes         = notes,
    installedAt   = installedAt.toEpochDay()
)
