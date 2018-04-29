package com.astrolucis.tests.viewModels

import com.astrolucis.core.BaseTest
import com.astrolucis.features.dailyPredictionList.DailyPredictionListViewModel
import com.astrolucis.features.login.LoginViewModel
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.TrampolineSchedulerRule
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class DailyPredictionListViewModelTest: BaseTest() {

    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    @After
    fun after(){
        StandAloneContext.closeKoin()
    }

    private fun initViewModel(): DailyPredictionListViewModel {
        return DailyPredictionListViewModel(RuntimeEnvironment.application)
    }

    @Test
    fun test_daily_predictions_rows_generated() {
        val dailyPredictionListViewModel = initViewModel()

        dailyPredictionListViewModel.initForm()

        val predictionDates = dailyPredictionListViewModel.listLiveData.value

        Assert.assertEquals(DailyPredictionListViewModel.VISIBLE_DATES, predictionDates?.size)

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        predictionDates?.forEach {
            Assert.assertEquals(calendar.time, it)
            calendar.add(Calendar.DATE, 1)
        }
    }

    @Test
    fun test_daily_prediction_clicked() {
        val dailyPredictionListViewModel = initViewModel()

        dailyPredictionListViewModel.initForm()

        val predictionDates = dailyPredictionListViewModel.listLiveData.value

        predictionDates?.first()?.let {
            dailyPredictionListViewModel.predictionSelected(it)

            Assert.assertEquals(dailyPredictionListViewModel.actionsLiveData.value,
                    Pair(DailyPredictionListViewModel.Action.GO_TO_DAILY_PREDICTION, it))
        }
    }
}