package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.dto.TreatmentDto
import com.example.apiarymanager.data.local.entity.TreatmentEntity
import com.example.apiarymanager.domain.model.Treatment
import java.time.LocalDate

// ─── DTO → Domain ────────────────────────────────────────────────────────────

fun TreatmentDto.toDomain(): Treatment = Treatment(
    id                      = id,
    hiveId                  = hiveId,
    date                    = LocalDate.parse(date),
    medicineType            = medicineType,
    dosage                  = dosage,
    applicationMethod       = applicationMethod,
    mortalityAfterTreatment = mortalityAfterTreatment
)

// ─── DTO → Entity ────────────────────────────────────────────────────────────

fun TreatmentDto.toEntity(): TreatmentEntity = TreatmentEntity(
    id                      = id,
    hiveId                  = hiveId,
    date                    = LocalDate.parse(date).toEpochDay(),
    medicineType            = medicineType,
    dosage                  = dosage,
    applicationMethod       = applicationMethod,
    mortalityAfterTreatment = mortalityAfterTreatment
)

// ─── Entity → Domain ─────────────────────────────────────────────────────────

fun TreatmentEntity.toDomain(): Treatment = Treatment(
    id                       = id,
    hiveId                   = hiveId,
    date                     = LocalDate.ofEpochDay(date),
    medicineType             = medicineType,
    dosage                   = dosage,
    applicationMethod        = applicationMethod,
    mortalityAfterTreatment  = mortalityAfterTreatment
)

fun Treatment.toEntity(): TreatmentEntity = TreatmentEntity(
    id                       = id,
    hiveId                   = hiveId,
    date                     = date.toEpochDay(),
    medicineType             = medicineType,
    dosage                   = dosage,
    applicationMethod        = applicationMethod,
    mortalityAfterTreatment  = mortalityAfterTreatment
)
