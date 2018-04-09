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
import com.astrolucis.utils.JWTUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

class AppRouter(private val preferences: Preferences, private val natalDateService: NatalDateService) {

    companion object {
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

        if (activityClass.isSubclassOf(ResetPasswordActivity::class)) {
            preferences.token = params[ResetPasswordActivity.TOKEN] as String
            val intent: Intent = Intent(fromActivity, ResetPasswordActivity::class.java).apply {
                putExtras(params)
            }
            startActivity(fromActivity, intent, delay)
        } else if (!JWTUtils.isLoggedIn(preferences.token)) {
            startActivity(fromActivity, Intent(fromActivity, LoginActivity::class.java), delay)
        } else {
            getUserData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { me ->
                                params.clear()
                                val intent: Intent = if (me != null) {
                                    params.putBoolean(HomeActivity.OPEN_NATAL_DATE, true)
                                    Intent(fromActivity, HomeActivity::class.java).apply {
                                        putExtras(params)
                                    }
                                } else {
                                    Intent(fromActivity, activityClass.java).apply {
                                        putExtras(params)
                                    }
                                }
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

    private fun getUserData(): Observable<UserFragment> {
        return if (preferences.me == null) {
            natalDateService.getAll()
                    .doOnNext { me -> preferences.me = me }
        } else {
            Observable.just(preferences.me)
        }
    }
}