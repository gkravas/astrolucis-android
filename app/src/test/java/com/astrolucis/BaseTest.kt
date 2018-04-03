package com.astrolucis

import android.content.res.Resources
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.Preferences
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.koin.test.KoinTest
import org.robolectric.RuntimeEnvironment

open class BaseTest: KoinTest {
    val resources: Resources get() = RuntimeEnvironment.application.resources

    fun initPreferences(token: String, userFragment: UserFragment? = null): Preferences {
        val preferences: Preferences = mock {
        }

        whenever(preferences.token).thenReturn(token)
        whenever(preferences.me).thenReturn(userFragment)
        return preferences
    }
}