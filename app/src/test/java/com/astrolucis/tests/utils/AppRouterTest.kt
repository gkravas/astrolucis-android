package com.astrolucis.tests.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.astrolucis.core.BaseActivity
import com.astrolucis.core.BaseTest
import com.astrolucis.features.dailyPredictionList.DailyPredictionListFragment
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.features.login.LoginActivity
import com.astrolucis.features.resetPassword.ResetPasswordActivity
import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.tests.utils.Constants.Companion.EXPIRED_JWT
import com.astrolucis.tests.utils.Constants.Companion.NON_EXPIRING_JWT
import com.astrolucis.tests.viewModels.NatalDateViewModelTest
import com.astrolucis.type.natalDatetypeEnumType
import com.astrolucis.utils.TrampolineSchedulerRule
import com.astrolucis.utils.routing.AppRouter
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.closeKoin
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.*


@RunWith(RobolectricTestRunner::class)
class AppRouterTest: BaseTest() {

    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    private lateinit var appRouter: AppRouter

    private fun initNatalDateService(returnNull: Boolean): NatalDateService {
        val observable = if (returnNull) {
            val natalDateFragment = NatalDateFragment("", 1, "me", "2018-03-22 00:00:00",
                    "Greece", true, natalDatetypeEnumType.freeSpirit)
            val natalDate = UserFragment.NatalDate("", UserFragment.NatalDate.Fragments(natalDateFragment))

            Observable.just(UserFragment("", "test@astrolucis.gr", "Greece", Arrays.asList(natalDate)))
        } else {
            Observable.error(RuntimeException())
        }
        return mock {
            on { getAll() } doReturn observable
        }
    }

    @After
    fun after(){
        closeKoin()
    }

    @Test
    fun test_parsing_resetPassword() {
        appRouter = AppRouter(initPreferences(Preferences.EMPTY_STRING))

        val tokenParamName = "t"
        val tokenParamValue = "this-is-a-token"
        val resetPasswordUri: Uri = Uri.parse("https://astrolucis.gr/resetPassword?$tokenParamName=$tokenParamValue")

        appRouter.identifyFrom(resetPasswordUri).also {
            assertEquals(it.kClass, ResetPasswordActivity::class)
            assert(it.bundle.containsKey(tokenParamName))
            assertEquals(it.bundle.get(tokenParamName), tokenParamValue)
        }
    }

    @Test
    fun test_parsing_null() {
        appRouter = AppRouter(initPreferences(Preferences.EMPTY_STRING))

        appRouter.identifyFrom(null).also {
            assertEquals(it.kClass, HomeActivity::class)
            assert(it.bundle.keySet().isEmpty())
        }
    }

    @Test
    fun test_go_to_home_logged_in_no_natal_date() {
        appRouter = AppRouter(initPreferences(NON_EXPIRING_JWT, createValidUser(false)))

        val activityController = Robolectric.buildActivity(BaseActivity::class.java)
                .create()
                .start()
        val activity = activityController.get()
        activityController.resume()

        appRouter.goTo(HomeActivity::class, activity, Bundle(), true)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(HomeActivity::class.java, shadowIntent.intentClass)

        assertTrue("Top Fragment is NatalDateFragment",
                startedIntent.extras.getBoolean(HomeActivity.OPEN_NATAL_DATE, false))
    }

    @Test
    fun test_go_to_home_logged_in_no_user_in_preferences() {
        appRouter = AppRouter(initPreferences(NON_EXPIRING_JWT, null))

        val activityController = Robolectric.buildActivity(BaseActivity::class.java)
                .create()
                .start()
        val activity = activityController.get()
        activityController.resume()

        appRouter.goTo(HomeActivity::class, activity, Bundle(), true)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(HomeActivity::class.java, shadowIntent.intentClass)

        assertTrue("Top Fragment is NatalDateFragment",
                startedIntent.extras.getBoolean(HomeActivity.OPEN_NATAL_DATE, false))
    }

    @Test
    fun test_go_to_home_logged_in_has_natal_date() {
        appRouter = AppRouter(initPreferences(NON_EXPIRING_JWT, createValidUser()))

        val activityController = Robolectric.buildActivity(BaseActivity::class.java)
                .newIntent(Intent())
                .create()
                .start()
        val activity = activityController.get()
        activityController.resume()

        appRouter.goTo(HomeActivity::class, activity, Bundle(), true)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(HomeActivity::class.java, shadowIntent.intentClass)

        assertTrue("Top Fragment is DailyPredictionListFragment", startedIntent.extras.isEmpty)
    }

    @Test
    fun test_go_to_home_no_logged_in() {
        appRouter = AppRouter(initPreferences(Preferences.EMPTY_STRING))

        val activityController = Robolectric.buildActivity(BaseActivity::class.java)
                .create()
                .start()
        val activity = activityController.get()
        activityController.resume()

        appRouter.goTo(HomeActivity::class, activity, Bundle(), true)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(LoginActivity::class.java, shadowIntent.intentClass)
    }

    @Test
    fun test_go_to_home_expired_jwt_token() {
        appRouter = AppRouter(initPreferences(EXPIRED_JWT))

        val activityController = Robolectric.buildActivity(BaseActivity::class.java)
                .create()
                .start()
        val activity = activityController.get()
        activityController.resume()

        appRouter.goTo(HomeActivity::class, activity, Bundle(), true)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(LoginActivity::class.java, shadowIntent.intentClass)
    }

    /*
    ToDo: I have to find a way to mock KeyStore via robolectric
    @Test
    fun test_go_to_reset_password_non_expired_jwt_token() {
        //app routers setup doesn't matter, it first checks for reset password
        appRouter = AppRouter(initPreferences(Preferences.EMPTY_STRING), initNatalDateService(false))

        val activityController = Robolectric.buildActivity(SplashActivity::class.java)
                .create()
                .newIntent(Intent().apply {
                    this.data = Uri.parse(RESET_PASSWORD_URI + NON_EXPIRING_JWT)
                })
                .start()
        val activity = activityController.get()
        activityController.start()

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(ResetPasswordActivity::class.java, shadowIntent.intentClass)
    }*/
}