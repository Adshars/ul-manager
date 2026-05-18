package com.example.apiarymanager.core.network

interface TokenProvider {
    suspend fun getToken(forceRefresh: Boolean = false): String?
}

/** Used before MSAL is wired up — sends no auth header. */
class NoOpTokenProvider : TokenProvider {
    override suspend fun getToken(forceRefresh: Boolean): String? = null
}
