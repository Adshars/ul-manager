package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.DashboardDto
import retrofit2.http.GET

interface DashboardApi {
    @GET("dashboard")
    suspend fun getDashboard(): DashboardDto
}
