package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CreateFeedingRequest
import com.example.apiarymanager.data.dto.FeedingDto
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.UpdateFeedingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedingApi {

    @GET("hives/{hiveId}/feedings")
    suspend fun getByHive(
        @Path("hiveId") hiveId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<FeedingDto>

    @GET("feedings/{id}")
    suspend fun getById(@Path("id") id: Long): Response<FeedingDto>

    @POST("feedings")
    suspend fun create(@Body body: CreateFeedingRequest): Response<FeedingDto>

    @PUT("feedings/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateFeedingRequest
    ): Response<FeedingDto>

    @DELETE("feedings/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>
}
