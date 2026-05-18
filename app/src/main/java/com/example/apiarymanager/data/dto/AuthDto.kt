package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val oid: String,
    val email: String,
    val displayName: String,
    val createdAt: String,
    val lastSeenAt: String
)

@Serializable
data class RegisterRequest(
    val displayName: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ForgotPasswordResponse(
    val selfServicePasswordResetUrl: String
)
