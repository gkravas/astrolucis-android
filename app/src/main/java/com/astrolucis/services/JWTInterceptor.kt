package com.astrolucis.services

import com.astrolucis.BuildConfig
import com.astrolucis.services.interfaces.Preferences
import okhttp3.Interceptor
import okhttp3.Response

class JWTInterceptor(val preferences: Preferences): Interceptor {

    companion object {
        private const val userAgentHeader: String = "User-Agent"
        private const val userAgentHeaderPrefix: String
                = "AstroLucis Android ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"

        private const val authorizationHeader: String = "Authorization"
        private const val authorizationHeaderPrefix: String = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
                .addHeader(userAgentHeader, userAgentHeaderPrefix)
                .addHeader(authorizationHeader, "$authorizationHeaderPrefix ${preferences.token}")
                .build()
        return chain.proceed(newRequest)
    }
}