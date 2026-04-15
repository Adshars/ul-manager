package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.ApiaryDto
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import com.example.apiarymanager.domain.model.Apiary
import java.time.LocalDate

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun ApiaryDto.toEntity(): ApiaryEntity = ApiaryEntity(
    id        = id,
    name      = name,
    location  = location,
    latitude  = latitude,
    longitude = longitude,
    notes     = notes,
    createdAt = LocalDate.parse(createdAt).toEpochDay()
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
