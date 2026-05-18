package com.example.apiarymanager.core.network

class ApiException(val code: Int, val rawBody: String?) : Exception("HTTP $code: $rawBody")
