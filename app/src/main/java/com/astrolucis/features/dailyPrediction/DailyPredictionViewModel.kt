package com.astrolucis.features.dailyPrediction

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import com.astrolucis.GetDailyPredictionQuery
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DailyPredictionViewModel : BaseViewModel {


    enum class Action {
        RATE_DAILY_PREDICTION,
        RATING_COMPLETE
    }


    companion object {
        const val ACCURACY_MULTIPLIER: Long = 20
    }

    private val natalDateService: NatalDateService
    private val disposables = CompositeDisposable()

    val dailyPredictionLiveData: MutableLiveData<GetDailyPredictionQuery.DailyPrediction> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Pair<Action, Any>> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val loading: ObservableField<Boolean>

    private var natalDateId: Long = 0
    private var date: String = ""

    constructor(application: Application, natalDateService: NatalDateService): super(application) {
        this.natalDateService = natalDateService
        this.loading = ObservableField()
    }

    fun loadPrediction(natalDateId: Long, date: String) {
        this.natalDateId = natalDateId
        this.date = date

//        dailyPredictionLiveData.value?.let {
//            dailyPredictionLiveData.value = it
//            return
//        }

        disposables.add(natalDateService.getDailyPrediction(natalDateId, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        { dailyPrediction ->
                            dailyPredictionLiveData.value = dailyPrediction
                            loading.set(false)
                        },
                        { throwable ->
                            messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                            loading.set(false)
                        }
                ))
    }

    fun openRateDialog() {
        dailyPredictionLiveData.value?.let {
            actionsLiveData.value = Pair(Action.RATE_DAILY_PREDICTION, it)
        }
    }

    fun onAccuracySubmission(accuracy: Long) {
        disposables.add(natalDateService.rateDailyPredectionAccuracy(natalDateId, date, accuracy * ACCURACY_MULTIPLIER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        {
                            dailyPredictionLiveData.value = GetDailyPredictionQuery
                                    .DailyPrediction("",
                                            it.accuracy(),
                                            dailyPredictionLiveData.value?.planetExplanations())

                            loading.set(false)
                            messagesLiveData.value = ErrorPresentation(R.string.dailyPrediction_ratingComplete,
                                    0, "", ErrorPresentation.Companion.Type.SNACK_BAR)
                        },
                        { throwable ->
                            messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                            loading.set(false)
                        }
                ))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}