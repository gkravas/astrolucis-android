package com.astrolucis.features.natalChart

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.core.BaseViewModel
import com.astrolucis.models.natalDate.Chart
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class NatalChartViewModel : BaseViewModel {

    enum class Action {
        NO,
        EDIT
    }

    private val userService: UserService
    private val natalDateService: NatalDateService
    private val preferences: Preferences

    private val disposables = CompositeDisposable()

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val chartLiveData: MutableLiveData<Chart> = MutableLiveData()

    constructor(application: Application, userService: UserService, natalDateService: NatalDateService, preferences: Preferences) : super(application) {
        this.userService = userService
        this.natalDateService = natalDateService
        this.preferences = preferences

        this.loading.postValue(false)
        loadNatalDate()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun loadNatalDate() {
        //local chart
        val chart: Chart? = preferences.me
                ?.natalDates()
                ?.firstOrNull()
                ?.fragments()
                ?.natalDateFragment()
                ?.chart()

            disposables.add(
                    if (chart == null) io.reactivex.Observable.just(chart) as Disposable else natalDateService.getAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { loading.value = true }
                    .subscribe(
                            { me ->
                                chartLiveData.value = me?.natalDates()
                                        ?.firstOrNull()
                                        ?.fragments()
                                        ?.natalDateFragment()
                                        ?.chart()
                                loading.value = false
                            },
                            { throwable ->
                                messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                                loading.value = false
                            }
                    ))
    }

    fun editNatalDate() {
        actionsLiveData.value = Action.EDIT
        actionsLiveData.value = Action.NO
    }
}
