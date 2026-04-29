package com.example.apiarymanager.domain.repository

import com.example.apiarymanager.domain.model.AnalysisRequest
import com.example.apiarymanager.domain.model.AnalysisResult

interface PhotoAnalysisRepository {
    suspend fun analyzePhoto(request: AnalysisRequest): AnalysisResult
}
