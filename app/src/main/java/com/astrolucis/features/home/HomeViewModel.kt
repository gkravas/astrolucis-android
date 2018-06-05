package com.astrolucis.features.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.BuildConfig
import com.astrolucis.core.BaseViewModel
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.google.firebase.analytics.FirebaseAnalytics

class HomeViewModel: BaseViewModel {

    companion object {
        const val UPDATE_DIALOG_ID: String = "updateDialogId"
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

    val viewState: MutableLiveData<ViewState> = MutableLiveData()

    constructor(application: Application, userService: UserService, preferences: Preferences) : super(application) {
        this.userService = userService
        this.preferences = preferences

        preferences.me?.let {
            FirebaseAnalytics.getInstance(application).setUserId(it.id().toString())
        }
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

    private fun getGoTo(viewState: ViewState): ViewState {
        if (preferences.me?.natalDates()?.isEmpty()!!) {
            return ViewState.STAY_THERE
        }
        return viewState
    }

    fun showUpgradeDialog(latestVersion: Long) {
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