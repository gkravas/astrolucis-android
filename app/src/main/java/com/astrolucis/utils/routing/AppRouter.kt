package com.astrolucis.utils.routing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.astrolucis.core.BaseActivity
import com.astrolucis.exceptions.RoutingException
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.features.login.LoginActivity
import com.astrolucis.features.resetPassword.ResetPasswordActivity
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class AppRouter(private val preferences: Preferences, private val natalDateService: NatalDateService) {

    companion object {
        const val minExpiredTimeSpan: Long = 60 * 60
        const val DELAY: Long = 1000
        const val NO_DELAY: Long = -1
        const val RESET_PASSWORD = "/resetPassword"
    }

    fun identifyFrom(uri: Uri?): RoutingParams {
        when (uri?.encodedPath) {
            RESET_PASSWORD -> {
                return RoutingParams(ResetPasswordActivity::class, uri)
            }
        }
        return RoutingParams(HomeActivity::class, null)
    }

    fun goTo(activityClass: KClass<*>, fromActivity: BaseActivity, params: Bundle = Bundle(), finishCurrent: Boolean = false) {
        if (activityClass.isSuperclassOf(BaseActivity::class)) {
            throw RoutingException()
        }

        val delay: Long = if (finishCurrent) DELAY else NO_DELAY

        if (!isLoggedIn(preferences.token)) {
            startActivity(fromActivity, Intent(fromActivity, LoginActivity::class.java), delay)
        } else {
            getUserData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { me ->
                                params.clear()
                                params.putBoolean(HomeActivity.openNatalDate, true)
                                val intent: Intent =// if (me != null) {
                                    Intent(fromActivity, HomeActivity::class.java).apply {
                                        putExtras(params)
                                    }
//                                } else {
//                                    Intent(fromActivity, activityClass.java).apply {
//                                        putExtras(params)
//                                    }
//
//                                }
                                startActivity(fromActivity, intent, delay)
                            },
                            {
                                startActivity(fromActivity, Intent(fromActivity, LoginActivity::class.java), delay)
                            }
                    )
        }
    }

    private fun startActivity(fromActivity: BaseActivity, intent: Intent, delay: Long) {
        fromActivity.startActivity(intent)
        if (delay != NO_DELAY) {
            Handler().postDelayed({ fromActivity.finish() }, delay)
        }
    }
    private fun isLoggedIn(token: String): Boolean {
        return try {
            !JWT(token).isExpired(minExpiredTimeSpan)
        } catch (e: Throwable) {
            when(e) {
                is DecodeException,
                is NullPointerException -> {
                    return false
                } else -> {
                    throw  e
                }
            }
        }
    }

    private fun getUserData(): Observable<UserFragment> {
        return if (preferences.me == null) {
            natalDateService.getAll()
                    .doOnNext { me -> preferences.me = me }
        } else {
            Observable.just(preferences.me)
        }
    }
}