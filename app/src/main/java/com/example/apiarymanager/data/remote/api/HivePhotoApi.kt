package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.AnalysisResultDto
import com.example.apiarymanager.data.dto.AnalyzePhotoRequest
import com.example.apiarymanager.data.dto.HivePhotoDto
import com.example.apiarymanager.data.dto.PagedResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface HivePhotoApi {

    @GET("hives/{hiveId}/photos")
    suspend fun getByHive(
        @Path("hiveId") hiveId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<HivePhotoDto>

    @Multipart
    @POST("hives/{hiveId}/photos")
    suspend fun upload(
        @Path("hiveId") hiveId: Long,
        @Part file: MultipartBody.Part,
        @Query("inspectionId") inspectionId: Long? = null
    ): Response<HivePhotoDto>

    @DELETE("hives/{hiveId}/photos/{photoId}")
    suspend fun delete(
        @Path("hiveId") hiveId: Long,
        @Path("photoId") photoId: Long
    ): Response<Unit>

    @POST("hives/{hiveId}/photos/{photoId}/analyze")
    suspend fun analyze(
        @Path("hiveId") hiveId: Long,
        @Path("photoId") photoId: Long,
        @Body body: AnalyzePhotoRequest
    ): AnalysisResultDto
}
