package com.astrolucis.core

import android.content.res.Resources
import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.tests.utils.Constants
import com.astrolucis.type.natalDatetypeEnumType
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.koin.test.KoinTest
import org.robolectric.RuntimeEnvironment
import java.util.*

open class BaseTest: KoinTest {
    val resources: Resources get() = RuntimeEnvironment.application.resources

    fun initPreferences(token: String = Preferences.EMPTY_STRING,
                        userFragment: UserFragment? = null): Preferences {
        val preferences: Preferences = mock {
        }

        whenever(preferences.token).thenReturn(token)
        whenever(preferences.me).thenReturn(userFragment)
        return preferences
    }

    fun createValidUser(hasNatalDate: Boolean = true): UserFragment {
        val natalDateFragment = NatalDateFragment(Constants.EMPTY, Constants.NATAL_DATE_ID,
                Constants.VALID_NAME, Constants.SERVER_SIDE_FULL_DATE,
                Constants.VALID_BIRTH_LOCATION, true, natalDatetypeEnumType.freeSpirit)
        val natalDate = UserFragment.NatalDate(Constants.EMPTY, UserFragment.NatalDate.Fragments(natalDateFragment))

        return UserFragment(Constants.EMPTY, Constants.TEST_EMAIL,
                Constants.VALID_LIVING_LOCATION,
                if (hasNatalDate) arrayListOf(natalDate) else arrayListOf() )
    }
}