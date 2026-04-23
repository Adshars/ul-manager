package com.example.apiarymanager.data.mapper

import com.example.apiarymanager.data.local.entity.TreatmentEntity
import com.example.apiarymanager.domain.model.Treatment
import java.time.LocalDate

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
