package com.example.apiarymanager.domain.usecase

import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SaveInspectionUseCase @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val hiveRepository: HiveRepository
) {
    suspend operator fun invoke(inspection: Inspection): Long {
        val savedId = if (inspection.id == 0L) {
            inspectionRepository.insertInspection(inspection)
        } else {
            inspectionRepository.updateInspection(inspection)
            inspection.id
        }

        if (inspection.newQueenYear != null) {
            val hive = hiveRepository.getHiveById(inspection.hiveId).first()
            if (hive != null) {
                hiveRepository.updateHive(hive.copy(queenYear = inspection.newQueenYear))
            }
        }

        return savedId
    }
}
