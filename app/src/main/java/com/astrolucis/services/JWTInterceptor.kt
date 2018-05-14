package com.astrolucis.services

import com.astrolucis.BuildConfig
import com.astrolucis.services.interfaces.Preferences
import okhttp3.Interceptor
import okhttp3.Response

class JWTInterceptor(val preferences: Preferences): Interceptor {

    companion object {
        private const val USER_AGENT_HEADER: String = "User-Agent"
        private const val USER_AGENT_HEADER_PREFIX: String
                = "AstroLucis Android ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        private const val AUTHORIZATION_HEADER: String = "Authorization"
        private const val AUTHORIZATION_HEADER_PREFIX: String = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
                .header(USER_AGENT_HEADER, USER_AGENT_HEADER_PREFIX)
                .addHeader(AUTHORIZATION_HEADER, "$AUTHORIZATION_HEADER_PREFIX ${preferences.token}")
                .build()
        return chain.proceed(newRequest)
    }
}