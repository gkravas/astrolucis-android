package com.astrolucis.features.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.core.BaseViewModel
import com.astrolucis.services.interfaces.UserService

class HomeViewModel: BaseViewModel {

    enum class ViewState {
        PROFILE,
        NATAL_DATE,
        DAILY_PREDICTION_LIST,
        LOGOUT;
    }

    private val userService: UserService

    val viewState: MutableLiveData<ViewState> = MutableLiveData()

    constructor(application: Application, userService: UserService) : super(application) {
        this.userService = userService
    }

    fun logout() {
        userService.logout()
        viewState.value = ViewState.LOGOUT
    }

    fun goToProfile() {
        viewState.value = ViewState.PROFILE
    }

    fun goToNatalDate() {
        viewState.value = ViewState.NATAL_DATE
    }

    fun goToDailyPrediction() {
        viewState.value = ViewState.DAILY_PREDICTION_LIST
    }
}