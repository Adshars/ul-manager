package com.example.apiarymanager.core.network

interface TokenProvider {
    suspend fun getToken(): String?
}

/** Used before MSAL is wired up — sends no auth header. */
class NoOpTokenProvider : TokenProvider {
    override suspend fun getToken(): String? = null
}
