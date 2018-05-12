package com.astrolucis.features.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.core.BaseViewModel
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService

class HomeViewModel: BaseViewModel {

    enum class ViewState {
        STAY_THERE,
        PROFILE,
        NATAL_DATE,
        DAILY_PREDICTION_LIST,
        LOGOUT;
    }

    private val userService: UserService
    private val preferences: Preferences

    val viewState: MutableLiveData<ViewState> = MutableLiveData()

    constructor(application: Application, userService: UserService, preferences: Preferences) : super(application) {
        this.userService = userService
        this.preferences = preferences
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

    fun getGoTo(viewState: ViewState): ViewState {
        if (preferences.me?.natalDates()?.isEmpty()!!) {
            return ViewState.STAY_THERE
        }
        return viewState
    }
}