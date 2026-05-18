package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.StatYearTotalsDto
import retrofit2.http.GET
import retrofit2.http.Query

interface StatsApi {

    @GET("stats/harvest")
    suspend fun getHarvestStats(
        @Query("year") year: Int,
        @Query("apiaryId") apiaryId: Long? = null
    ): StatYearTotalsDto

    @GET("stats/feeding")
    suspend fun getFeedingStats(
        @Query("year") year: Int,
        @Query("apiaryId") apiaryId: Long? = null
    ): StatYearTotalsDto
}
