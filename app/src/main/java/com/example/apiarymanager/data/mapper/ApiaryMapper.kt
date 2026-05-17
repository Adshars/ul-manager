package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.ApiaryDto
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import com.example.apiarymanager.domain.model.Apiary
import java.time.LocalDate

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun ApiaryDto.toDomain(): Apiary = Apiary(
    id        = id,
    name      = name,
    location  = location,
    latitude  = latitude,
    longitude = longitude,
    notes     = notes,
    createdAt = createdAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) } ?: LocalDate.now()
)

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun ApiaryDto.toEntity(): ApiaryEntity = ApiaryEntity(
    id        = id,
    name      = name,
    location  = location,
    latitude  = latitude,
    longitude = longitude,
    notes     = notes,
    createdAt = createdAt.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it).toEpochDay() } ?: LocalDate.now().toEpochDay()
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun ApiaryEntity.toDomain(): Apiary = Apiary(
    id        = id,
    name      = name,
    location  = location,
    latitude  = latitude,
    longitude = longitude,
    notes     = notes,
    createdAt = LocalDate.ofEpochDay(createdAt)
)

// ─── Domain → Entity (for local CRUD from UI) ─────────────────────────────────

fun Apiary.toEntity(): ApiaryEntity = ApiaryEntity(
    id        = id,
    name      = name,
    location  = location,
    latitude  = latitude,
    longitude = longitude,
    notes     = notes,
    createdAt = createdAt.toEpochDay()
)
