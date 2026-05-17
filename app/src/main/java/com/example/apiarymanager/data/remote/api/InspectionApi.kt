package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CreateInspectionRequest
import com.example.apiarymanager.data.dto.InspectionDto
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.UpdateInspectionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InspectionApi {

    @GET("hives/{hiveId}/inspections")
    suspend fun getByHive(
        @Path("hiveId") hiveId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<InspectionDto>

    @GET("inspections/{id}")
    suspend fun getById(@Path("id") id: Long): Response<InspectionDto>

    @POST("inspections")
    suspend fun create(@Body body: CreateInspectionRequest): Response<InspectionDto>

    @PUT("inspections/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateInspectionRequest
    ): Response<InspectionDto>

    @DELETE("inspections/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>
}
