package com.astrolucis.services.interfaces

import com.astrolucis.UpdateUserMutation
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.repsonses.LoginResponse
import io.reactivex.Observable

interface UserService {

    companion object {
        val TAG: String = UserService::class.toString()
    }

    fun register(email: String, password: String): Observable<String>
    fun login(email: String, password: String): Observable<LoginResponse>
    fun fbLogin(fbToken: String): Observable<LoginResponse>
    fun sendResetEmail(email: String): Observable<String>
    fun resetPassword(password: String): Observable<String>
    fun updateLivingLocation(livingLocation: String): Observable<UserFragment?>
    fun logout()
}