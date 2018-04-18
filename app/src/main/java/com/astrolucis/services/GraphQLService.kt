package com.astrolucis.services

import com.apollographql.apollo.ApolloClient
import com.astrolucis.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.cert.CertificateException


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
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(JWTInterceptor(preferences))
                    .build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }


    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { hostname, session -> true }

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            return builder
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