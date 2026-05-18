package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.ApiaryDto
import com.example.apiarymanager.data.dto.CreateApiaryRequest
import com.example.apiarymanager.data.dto.HiveDto
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.StatYearTotalsDto
import com.example.apiarymanager.data.dto.UpdateApiaryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiaryApi {

    @GET("apiaries")
    suspend fun getAll(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<ApiaryDto>

    @GET("apiaries/{id}")
    suspend fun getById(@Path("id") id: Long): Response<ApiaryDto>

    @POST("apiaries")
    suspend fun create(@Body body: CreateApiaryRequest): Response<ApiaryDto>

    @PUT("apiaries/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateApiaryRequest
    ): Response<ApiaryDto>

    @DELETE("apiaries/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>

    @GET("apiaries/{id}/hives")
    suspend fun getHives(
        @Path("id") apiaryId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<HiveDto>

    @GET("apiaries/{id}/stats/harvest")
    suspend fun getHarvestStats(
        @Path("id") apiaryId: Long,
        @Query("year") year: Int
    ): StatYearTotalsDto

    @GET("apiaries/{id}/stats/feeding")
    suspend fun getFeedingStats(
        @Path("id") apiaryId: Long,
        @Query("year") year: Int
    ): StatYearTotalsDto
}
