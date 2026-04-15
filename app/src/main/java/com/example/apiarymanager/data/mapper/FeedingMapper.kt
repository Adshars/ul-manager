package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.local.entity.FeedingEntity
import com.example.apiarymanager.domain.model.Feeding
import java.time.LocalDate

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
