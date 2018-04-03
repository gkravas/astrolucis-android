package com.astrolucis.services

import com.astrolucis.di.App
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.Preferences.Companion.EMPTY_STRING
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore

class Preferences: Preferences {

    private enum class KEYS constructor(private val text: String) {
        TOKEN("TOKEN");

        override fun toString(): String {
            return text
        }
    }

    private val store: SecuredPreferenceStore

    override var me: UserFragment? = null
    override var token: String
        get() {
            return store.getString(KEYS.TOKEN.toString(), EMPTY_STRING)
        }
        set(value) {
            saveString(KEYS.TOKEN.toString(), value)
        }

    constructor(app: App) {
        SecuredPreferenceStore.init(app, DefaultRecoveryHandler())
        store = SecuredPreferenceStore.getSharedInstance()
    }

    private fun saveString(name: String, value: String) {
        store.edit()
                .putString(name, value)
                .apply()
    }

    override fun reset() {
        token = EMPTY_STRING
    }
}