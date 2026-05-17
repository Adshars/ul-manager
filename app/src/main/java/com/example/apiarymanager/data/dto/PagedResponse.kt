package com.example.apiarymanager.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageSize: Int = 50,
    val pageNumber: Int = 1,
    val totalPages: Int = 1
)
