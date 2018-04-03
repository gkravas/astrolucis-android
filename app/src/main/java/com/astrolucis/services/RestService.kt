package com.astrolucis.services

import com.astrolucis.BuildConfig
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.util.concurrent.TimeUnit

class RestService {

    companion object {
        private const val CONNECTION_TIME_OUT: Long = 60
        private const val READ_TIME_OUT: Long = 20
        private const val WRITE_TIME_OUT: Long = 20
    }


    private val cookieManager: CookieManager
    private val preferences: com.astrolucis.services.interfaces.Preferences
    val authenticationAPI: UserAPI

    constructor(preferences: com.astrolucis.services.interfaces.Preferences, cookieManager: CookieManager) {
        this.preferences = preferences
        this.cookieManager = cookieManager

        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(Boolean::class.java, GSONIntToBooleanAdapter())
                .registerTypeAdapter(Boolean::class.javaPrimitiveType, GSONIntToBooleanAdapter())
                .create()

        authenticationAPI = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(getHttpClientBuilder())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(UserAPI::class.java)
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
                    .cookieJar(JavaNetCookieJar(cookieManager))
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(JWTInterceptor(preferences))
                    .build()


        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}