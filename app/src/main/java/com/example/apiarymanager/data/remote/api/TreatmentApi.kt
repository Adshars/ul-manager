package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CreateTreatmentRequest
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.TreatmentDto
import com.example.apiarymanager.data.dto.UpdateTreatmentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TreatmentApi {

    @GET("hives/{hiveId}/treatments")
    suspend fun getByHive(
        @Path("hiveId") hiveId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<TreatmentDto>

    @GET("treatments/{id}")
    suspend fun getById(@Path("id") id: Long): Response<TreatmentDto>

    @POST("treatments")
    suspend fun create(@Body body: CreateTreatmentRequest): Response<TreatmentDto>

    @PUT("treatments/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateTreatmentRequest
    ): Response<TreatmentDto>

    @DELETE("treatments/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>
}
