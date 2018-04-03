package com.astrolucis.services

import com.astrolucis.services.repsonses.LoginResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface UserAPI {
    @FormUrlEncoded
    @POST("/api/v1/auth/login")
    fun login(@Field("email") email: String?,
                       @Field("password") password: String?,
                       @Field("fbToken") fbToken: String?): Observable<LoginResponse>

    @FormUrlEncoded
    @POST("/api/v1/auth/fbLogin")
    fun fbLogin(@Field("fbToken") fbToken: String?): Observable<LoginResponse>

    @FormUrlEncoded
    @POST("/api/v1/auth/register")
    fun register(@Field("email") email: String,
                          @Field("password") password: String): Observable<String>

    @FormUrlEncoded
    @POST("/api/v1/auth/resetPassword")
    fun resetPassword(@Field("password") password: String): Observable<String>

    @FormUrlEncoded
    @POST("/api/v1/auth/sendResetEmail")
    fun sendResetEmail(@Field("email") email: String): Observable<String>

    @FormUrlEncoded
    @POST("/api/v1/auth/changeEmail")
    fun changeEmail(@Field("email") email: String): Observable<String>
}