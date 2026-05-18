package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CreateHiveRequest
import com.example.apiarymanager.data.dto.HiveDto
import com.example.apiarymanager.data.dto.UpdateHiveRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HiveApi {

    @GET("hives/{id}")
    suspend fun getById(@Path("id") id: Long): Response<HiveDto>

    @GET("hives/by-qr/{code}")
    suspend fun getByQrCode(@Path("code") code: String): Response<HiveDto>

    @GET("hives/queen-years/distinct")
    suspend fun getDistinctQueenYears(): List<Int>

    @POST("hives")
    suspend fun create(@Body body: CreateHiveRequest): Response<HiveDto>

    @PUT("hives/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateHiveRequest
    ): Response<HiveDto>

    @PUT("hives/{id}/qr")
    suspend fun regenerateQr(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<HiveDto>

    @DELETE("hives/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>
}
