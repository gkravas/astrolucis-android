package com.astrolucis.services.interfaces

import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.NatalDateService
import io.reactivex.Observable

interface NatalDateService {

    companion object {
        val TAG: String = NatalDateService::class.toString()
    }

    fun getAll(): Observable<UserFragment>

    fun createNatalDateMutation(date: String, location: String, name: String,
                                primary: Boolean, type: String): Observable<NatalDateFragment?>?

    fun updateNatalDateMutation(id: Long, date: String, location: String, name: String,
                                primary: Boolean, type: String): Observable<NatalDateFragment>?
}