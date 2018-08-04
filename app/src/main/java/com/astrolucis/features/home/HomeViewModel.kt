package com.astrolucis.features.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.BuildConfig
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class HomeViewModel: BaseViewModel {

    companion object {
        const val DAILY: String = "daily"
        const val DAILY_EL: String = "daily_el"
        const val DAILY_EN: String = "daily_en"
        const val GREEK_LANGUAGE: String = "el"
        const val LATEST_VERSION: String = "latest_version_android"
        const val OS: String = "android"

        const val UPDATE_DIALOG_ID: String = "updateDialogId"
        const val USER_LANGUAGE: String = "UserLanguage"
    }
    enum class ViewState {
        STAY_THERE,
        PROFILE,
        NATAL_DATE,
        DAILY_PREDICTION_LIST,
        SHOW_UPDATE_DIALOG,
        GO_TO_GOOGLE_PLAY,
        LOGOUT;
    }

    private val userService: UserService
    private val preferences: Preferences
    private val disposables = CompositeDisposable()

    val viewState: MutableLiveData<ViewState> = MutableLiveData()

    constructor(application: Application, userService: UserService, preferences: Preferences) : super(application) {
        this.userService = userService
        this.preferences = preferences

        preferences.me?.let { userFragment ->
            FirebaseAnalytics.getInstance(application).also { firebaseAnalytics ->
                firebaseAnalytics.setUserId(userFragment.id().toString())
                firebaseAnalytics.setUserProperty(USER_LANGUAGE, Locale.getDefault().language)
            }
        }
    }

    private fun registerToken(token: String, language: String) {
        disposables.add(userService.registerFirebaseToken(token, language, OS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { })
    }

    fun logout() {
        userService.logout()
        viewState.value = ViewState.LOGOUT
    }

    fun goToProfile() {
        viewState.value = getGoTo(ViewState.PROFILE)
    }

    fun goToNatalDate() {
        viewState.value = getGoTo(ViewState.NATAL_DATE)
    }

    fun goToDailyPrediction() {
        viewState.value = getGoTo(ViewState.DAILY_PREDICTION_LIST)
    }

    fun initFirebaseRemoteConfig() {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        firebaseRemoteConfig.setConfigSettings(configSettings)
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (firebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }


        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener {
                    firebaseRemoteConfig.activateFetched()
                    showUpgradeDialog(firebaseRemoteConfig.getLong(LATEST_VERSION))
                }
    }

    fun initFirebaseMessaging() {
        val language = Locale.getDefault().language
        if (preferences.dailyNotifications) {
            FirebaseMessaging.getInstance().subscribeToTopic(DAILY)
            if (language == GREEK_LANGUAGE) {
                FirebaseMessaging.getInstance().subscribeToTopic(DAILY_EL)
            } else {
                FirebaseMessaging.getInstance().subscribeToTopic(DAILY_EN)
            }
        }
        if (preferences.personalNotifications) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                registerToken(it.token, language)
            }
        }
    }

    private fun getGoTo(viewState: ViewState): ViewState {
        if (preferences.me?.natalDates()?.isEmpty()!!) {
            return ViewState.STAY_THERE
        }
        return viewState
    }

    private fun showUpgradeDialog(latestVersion: Long) {
        if (latestVersion > BuildConfig.VERSION_CODE) {
            viewState.value =  ViewState.SHOW_UPDATE_DIALOG
        }
    }

    override fun onDialogAction(id: String, positive: Boolean) {
        if (!positive) {
            return
        }
        when(id) {
            UPDATE_DIALOG_ID -> {
                viewState.value =  ViewState.GO_TO_GOOGLE_PLAY
            }
        }
    }
}