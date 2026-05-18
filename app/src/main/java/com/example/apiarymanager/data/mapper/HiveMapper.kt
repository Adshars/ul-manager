package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.core.util.parseEnum
import com.example.apiarymanager.data.dto.CreateHiveRequest
import com.example.apiarymanager.data.dto.HiveDto
import com.example.apiarymanager.data.dto.UpdateHiveRequest
import com.example.apiarymanager.data.local.entity.HiveEntity
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.HiveStatus
import java.time.LocalDate

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun HiveDto.toDomain(): Hive = Hive(
    id            = id,
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    queenYear     = queenYear,
    status        = parseEnum(status, HiveStatus.ACTIVE),
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin,
    notes         = notes,
    installedAt   = installedAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) } ?: LocalDate.now(),
    qrCode        = qrCode,
    latitude      = latitude,
    longitude     = longitude
)

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun HiveDto.toEntity(): HiveEntity = HiveEntity(
    id            = id,
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    queenYear     = queenYear,
    status        = status,
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin,
    notes         = notes,
    installedAt   = installedAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it).toEpochDay() } ?: 0L,
    qrCode        = qrCode,
    latitude      = latitude,
    longitude     = longitude
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun HiveEntity.toDomain(): Hive = Hive(
    id            = id,
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    queenYear     = queenYear,
    status        = parseEnum(status, HiveStatus.ACTIVE),
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin,
    notes         = notes,
    installedAt   = LocalDate.ofEpochDay(installedAt),
    qrCode        = qrCode,
    latitude      = latitude,
    longitude     = longitude
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
    installedAt   = installedAt.toEpochDay(),
    qrCode        = qrCode,
    latitude      = latitude,
    longitude     = longitude
)

fun Hive.toCreateRequest() = CreateHiveRequest(
    apiaryId      = apiaryId,
    number        = number,
    name          = name,
    status        = status.ordinal,
    installedAt   = installedAt.toString(),
    queenYear     = queenYear,
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin.takeIf { it.isNotBlank() },
    notes         = notes.takeIf { it.isNotBlank() },
    latitude      = latitude,
    longitude     = longitude
)

fun Hive.toUpdateRequest() = UpdateHiveRequest(
    number        = number,
    name          = name,
    status        = status.ordinal,
    installedAt   = installedAt.toString(),
    queenYear     = queenYear,
    frameType     = frameType,
    superboxCount = superboxCount,
    queenOrigin   = queenOrigin.takeIf { it.isNotBlank() },
    notes         = notes.takeIf { it.isNotBlank() },
    latitude      = latitude,
    longitude     = longitude
)
