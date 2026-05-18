package com.example.apiarymanager.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.getToken() }
        val request = buildRequest(chain, token)
        val response = chain.proceed(request)

        if (response.code == 401 && token != null) {
            response.close()
            val freshToken = runBlocking { tokenProvider.getToken(forceRefresh = true) }
            return chain.proceed(buildRequest(chain, freshToken))
        }

        return response
    }

    private fun buildRequest(chain: Interceptor.Chain, token: String?) =
        if (token != null)
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        else
            chain.request()
}
