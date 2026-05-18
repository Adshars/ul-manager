package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CreateHoneyHarvestRequest
import com.example.apiarymanager.data.dto.HoneyHarvestDto
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.UpdateHoneyHarvestRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HoneyHarvestApi {

    @GET("hives/{hiveId}/honey-harvests")
    suspend fun getByHive(
        @Path("hiveId") hiveId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<HoneyHarvestDto>

    @GET("honey-harvests/{id}")
    suspend fun getById(@Path("id") id: Long): Response<HoneyHarvestDto>

    @POST("honey-harvests")
    suspend fun create(@Body body: CreateHoneyHarvestRequest): Response<HoneyHarvestDto>

    @PUT("honey-harvests/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateHoneyHarvestRequest
    ): Response<HoneyHarvestDto>

    @DELETE("honey-harvests/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>
}
