package com.example.apiarymanager.data.repository

import com.example.apiarymanager.data.dto.AnalyzePhotoRequest
import com.example.apiarymanager.data.remote.source.HivePhotoSource
import com.example.apiarymanager.domain.model.AnalysisRequest
import com.example.apiarymanager.domain.model.AnalysisResult
import com.example.apiarymanager.domain.repository.PhotoAnalysisRepository
import javax.inject.Inject

class PhotoAnalysisRepositoryImpl @Inject constructor(
    private val hivePhotoSource: HivePhotoSource
) : PhotoAnalysisRepository {

    override suspend fun analyzePhoto(request: AnalysisRequest): AnalysisResult {
        val remoteId = request.photo.remoteId
            ?: return AnalysisResult(isSuccess = false, message = "Zdjęcie nie zostało jeszcze przesłane na serwer.")

        return hivePhotoSource.analyze(
            hiveId   = request.photo.hiveId,
            photoId  = remoteId,
            request  = AnalyzePhotoRequest(type = request.type.name, note = request.note)
        ).fold(
            onSuccess = { dto -> AnalysisResult(isSuccess = dto.isSuccess, message = dto.message) },
            onFailure = { e  -> AnalysisResult(isSuccess = false, message = e.message ?: "Błąd analizy") }
        )
    }
}
