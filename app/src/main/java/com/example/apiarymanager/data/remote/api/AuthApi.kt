package com.example.apiarymanager.data.remote.api

import com.example.apiarymanager.data.dto.ForgotPasswordRequest
import com.example.apiarymanager.data.dto.ForgotPasswordResponse
import com.example.apiarymanager.data.dto.RegisterRequest
import com.example.apiarymanager.data.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest = RegisterRequest()): UserProfileDto

    @POST("auth/login")
    suspend fun login(): UserProfileDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ForgotPasswordResponse

    @GET("me")
    suspend fun me(): UserProfileDto
}
