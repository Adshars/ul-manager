package com.example.apiarymanager.domain.usecase

import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.repository.InspectionRepository
import javax.inject.Inject

/**
 * Encapsulates the "create or update inspection" business rule:
 *  - id == 0  → insert (new inspection)
 *  - id != 0  → update (edit existing)
 *
 * Returns the persisted inspection id.
 */
class SaveInspectionUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(inspection: Inspection): Long =
        if (inspection.id == 0L) {
            repository.insertInspection(inspection)
        } else {
            repository.updateInspection(inspection)
            inspection.id
        }
}
