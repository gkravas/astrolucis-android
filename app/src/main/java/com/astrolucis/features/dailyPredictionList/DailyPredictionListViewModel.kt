package com.astrolucis.features.dailyPredictionList

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.astrolucis.core.BaseViewModel
import com.astrolucis.features.natalDate.NatalDateViewModel
import java.util.*
import kotlin.collections.ArrayList

class DailyPredictionListViewModel : BaseViewModel {

    companion object {
        public const val VISIBLE_DATES: Int = 60
    }
    enum class Action {
        GO_TO_DAILY_PREDICTION
    }

    private var predictionDates: ArrayList<Date> = arrayListOf()

    val listLiveData: MutableLiveData<ArrayList<Date>> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Pair<Action, Any>> = MutableLiveData()

    constructor(application: Application): super(application) {

    }

    fun initForm() {
        if (predictionDates.isEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            predictionDates = arrayListOf()

            for (i in 0 until VISIBLE_DATES) {
                predictionDates.add(calendar.time)
                calendar.add(Calendar.DATE, 1)
            }
        }

        listLiveData.value = predictionDates
    }

    fun predictionSelected(date: Date) {
        actionsLiveData.value = Pair(Action.GO_TO_DAILY_PREDICTION, date)
    }
}