package com.astrolucis.services.interfaces

import com.astrolucis.fragment.UserFragment

interface Preferences {
    companion object {
        const val EMPTY_STRING = ""
    }

    var me: UserFragment?
    var token: String
    fun reset()
}
