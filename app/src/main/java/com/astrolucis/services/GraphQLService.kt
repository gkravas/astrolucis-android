package com.astrolucis.services

import com.apollographql.apollo.ApolloClient
import com.astrolucis.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


class GraphQLService {
    companion object {
        private const val CONNECTION_TIME_OUT: Long = 60
        private const val READ_TIME_OUT: Long = 20
        private const val WRITE_TIME_OUT: Long = 20
    }

    val preferences: com.astrolucis.services.interfaces.Preferences
    val apolloClient: ApolloClient

    constructor(preferences: com.astrolucis.services.interfaces.Preferences) {
        this.preferences = preferences
        this.apolloClient = ApolloClient.builder()
                .serverUrl(BuildConfig.BASE_URL+ "/graphql")
                .okHttpClient(getHttpClientBuilder())
                .build()
    }

    private fun getHttpClientBuilder(): OkHttpClient {
        try {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            return OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .readTimeout(READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(JWTInterceptor(preferences))
                    .build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }


}