package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.CreateFeedingRequest
import com.example.apiarymanager.data.dto.FeedingDto
import com.example.apiarymanager.data.dto.UpdateFeedingRequest
import com.example.apiarymanager.data.local.entity.FeedingEntity
import com.example.apiarymanager.domain.model.Feeding
import java.time.LocalDate

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun FeedingDto.toDomain(): Feeding = Feeding(
    id                = id,
    hiveId            = hiveId,
    date              = LocalDate.parse(date),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod
)

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun FeedingDto.toEntity(): FeedingEntity = FeedingEntity(
    id                = id,
    hiveId            = hiveId,
    date              = LocalDate.parse(date).toEpochDay(),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun FeedingEntity.toDomain(): Feeding = Feeding(
    id                = id,
    hiveId            = hiveId,
    date              = LocalDate.ofEpochDay(date),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod
)

fun Feeding.toEntity(): FeedingEntity = FeedingEntity(
    id                = id,
    hiveId            = hiveId,
    date              = date.toEpochDay(),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod
)

fun Feeding.toCreateRequest() = CreateFeedingRequest(
    hiveId            = hiveId,
    date              = date.toString(),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod.takeIf { it.isNotBlank() }
)

fun Feeding.toUpdateRequest() = UpdateFeedingRequest(
    date              = date.toString(),
    foodType          = foodType,
    weightKg          = weightKg,
    applicationMethod = applicationMethod.takeIf { it.isNotBlank() }
)
