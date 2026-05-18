package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.CompleteTaskRequest
import com.example.apiarymanager.data.dto.CreateTaskRequest
import com.example.apiarymanager.data.dto.PagedResponse
import com.example.apiarymanager.data.dto.TaskDto
import com.example.apiarymanager.data.dto.UpdateTaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {

    @GET("tasks")
    suspend fun getAll(
        @Query("apiaryId") apiaryId: Long? = null,
        @Query("hiveId") hiveId: Long? = null,
        @Query("dueOnOrBefore") dueOnOrBefore: String? = null,
        @Query("completed") completed: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PagedResponse<TaskDto>

    @GET("tasks/{id}")
    suspend fun getById(@Path("id") id: Long): Response<TaskDto>

    @POST("tasks")
    suspend fun create(@Body body: CreateTaskRequest): Response<TaskDto>

    @PUT("tasks/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: UpdateTaskRequest
    ): Response<TaskDto>

    @DELETE("tasks/{id}")
    suspend fun delete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String
    ): Response<Unit>

    @POST("tasks/{id}/complete")
    suspend fun complete(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
        @Body body: CompleteTaskRequest
    ): Response<TaskDto>
}
