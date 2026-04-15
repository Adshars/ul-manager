package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.local.entity.HoneyHarvestEntity
import com.example.apiarymanager.domain.model.HoneyHarvest
import java.time.LocalDate

fun HoneyHarvestEntity.toDomain(): HoneyHarvest = HoneyHarvest(
    id        = id,
    hiveId    = hiveId,
    date      = LocalDate.ofEpochDay(date),
    honeyType = honeyType,
    weightKg  = weightKg,
    notes     = notes
)

fun HoneyHarvest.toEntity(): HoneyHarvestEntity = HoneyHarvestEntity(
    id        = id,
    hiveId    = hiveId,
    date      = date.toEpochDay(),
    honeyType = honeyType,
    weightKg  = weightKg,
    notes     = notes
)
