package com.example.apiarymanager.data.repository

import com.example.apiarymanager.domain.model.AnalysisRequest
import com.example.apiarymanager.domain.model.AnalysisResult
import com.example.apiarymanager.domain.model.AnalysisType
import com.example.apiarymanager.domain.repository.PhotoAnalysisRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakePhotoAnalysisRepositoryImpl @Inject constructor() : PhotoAnalysisRepository {

    override suspend fun analyzePhoto(request: AnalysisRequest): AnalysisResult {
        delay(3_000)
        return when (request.type) {
            AnalysisType.FIND_QUEEN ->
                AnalysisResult(
                    isSuccess = true,
                    message   = "Analiza zakończona pomyślnie. Królowa wykryta w centralnej części kadru. Stan: aktywna, czerwi widoczne."
                )
            AnalysisType.DETECT_VARROA ->
                AnalysisResult(
                    isSuccess = true,
                    message   = "Analiza zakończona. Wykryto 3 osobniki Varroa destructor na 100 pszczół. Zalecane leczenie preparatem kwasem szczawiowym."
                )
        }
    }
}
