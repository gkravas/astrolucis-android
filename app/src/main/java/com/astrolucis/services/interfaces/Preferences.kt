package com.astrolucis.services.interfaces

import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.Preferences

interface Preferences {
    companion object {
        const val EMPTY_STRING = ""
    }

    var me: UserFragment?
    var token: String
    var dailyNotifications: Boolean
    var personalNotifications: Boolean
    fun reset()
}
