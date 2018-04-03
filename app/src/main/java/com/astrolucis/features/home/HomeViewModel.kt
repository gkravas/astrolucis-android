package com.astrolucis.features.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.core.BaseViewModel
import com.astrolucis.services.interfaces.UserService

class HomeViewModel: BaseViewModel {

    enum class ViewState {
        PROFILE,
        NATAL_DATE,
        DAILY_PREDICTIONS,
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
}