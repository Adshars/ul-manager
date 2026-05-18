package com.example.apiarymanager.core.network

import com.example.apiarymanager.core.auth.MsalAuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MsalTokenProvider @Inject constructor(
    private val msalAuthManager: MsalAuthManager
) : TokenProvider {
    override suspend fun getToken(forceRefresh: Boolean): String? =
        msalAuthManager.getToken(forceRefresh)
}
