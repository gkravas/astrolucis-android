package com.astrolucis.tests.viewModels

import com.astrolucis.BR
import com.astrolucis.GetDailyPredictionQuery
import com.astrolucis.R
import com.astrolucis.RateDailyPredectionAccuracyMutation
import com.astrolucis.core.BaseTest
import com.astrolucis.features.dailyPrediction.DailyPredictionViewModel
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.utils.ErrorFactory
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.TrampolineSchedulerRule
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.collections.ArrayList

@RunWith(RobolectricTestRunner::class)
class DailyPredictionViewModelTest: BaseTest() {

    companion object {
        const val NATAL_DATE_ID: Long = 1
        const val DAILY_PREDICTION_DATE_SUCCESS: String = "1984-08-16"
        const val DAILY_PREDICTION_DATE_FAIL: String = "1949-29-05"
        const val RATE_VALUE_SUCCESS: Long = 5
        const val RATE_VALUE_FAIL: Long = 1
        const val DEFAULT_ACCURACY: Long = 10
        const val EXPLANATION_TITLE: String = "title"
        const val EXPLANATION_LEMMA: String = "lemma"
    }
    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    @After
    fun after(){
        StandAloneContext.closeKoin()
    }

    private fun initViewModel(): DailyPredictionViewModel {
        return DailyPredictionViewModel(RuntimeEnvironment.application, initNatalDateService())
    }

    private fun initNatalDateService(): NatalDateService {
        val explanations: ArrayList<GetDailyPredictionQuery.PlanetExplanation> = arrayListOf()
        explanations.add(GetDailyPredictionQuery.PlanetExplanation("", EXPLANATION_TITLE, EXPLANATION_LEMMA))
        val dailyPrediction = GetDailyPredictionQuery.DailyPrediction("", DEFAULT_ACCURACY, explanations)

        val rateDailyPredectionAccuracy = RateDailyPredectionAccuracyMutation.RateDailyPredectionAccuracy("", RATE_VALUE_SUCCESS * DailyPredictionViewModel.ACCURACY_MULTIPLIER)

        return mock {
            on { getDailyPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS) } doReturn Observable.just(dailyPrediction)
            on { getDailyPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_FAIL) } doReturn Observable.error(ErrorFactory.createServerError())
            on { rateDailyPredectionAccuracy(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS, RATE_VALUE_SUCCESS * DailyPredictionViewModel.ACCURACY_MULTIPLIER) } doReturn Observable.just(rateDailyPredectionAccuracy)
            on { rateDailyPredectionAccuracy(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS, RATE_VALUE_FAIL * DailyPredictionViewModel.ACCURACY_MULTIPLIER) } doReturn Observable.error(ErrorFactory.createServerError())
        }
    }

    @Test
    fun test_daily_prediction_fetch_success() {
        val dailyPredictionViewModel = initViewModel()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        dailyPredictionViewModel.loading.addOnPropertyChangedCallback(listener)

        dailyPredictionViewModel.loadPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS)
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(dailyPredictionViewModel.loading, BR._all)

        Assert.assertEquals(null, dailyPredictionViewModel.messagesLiveData.value)
        Assert.assertEquals(null, dailyPredictionViewModel.actionsLiveData.value)
        dailyPredictionViewModel.dailyPredictionLiveData.value?.let {
            Assert.assertEquals(DEFAULT_ACCURACY, it.accuracy())
            Assert.assertEquals(1, it.planetExplanations()?.size)
            Assert.assertEquals(EXPLANATION_TITLE, it.planetExplanations()?.get(0)?.title())
            Assert.assertEquals(EXPLANATION_LEMMA, it.planetExplanations()?.get(0)?.lemma())
        }
    }

    @Test
    fun test_daily_prediction_fetch_fail() {
        val dailyPredictionViewModel = initViewModel()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        dailyPredictionViewModel.loading.addOnPropertyChangedCallback(listener)

        dailyPredictionViewModel.loadPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_FAIL)
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(dailyPredictionViewModel.loading, BR._all)

        Assert.assertEquals(ErrorPresentation(R.string.error_defaultTitle, R.string.error_general), dailyPredictionViewModel.messagesLiveData.value)
        Assert.assertEquals(null, dailyPredictionViewModel.actionsLiveData.value)
        Assert.assertEquals(null, dailyPredictionViewModel.dailyPredictionLiveData.value)
    }

    @Test
    fun test_show_rate_dialog() {
        val dailyPredictionViewModel = initViewModel()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        dailyPredictionViewModel.loading.addOnPropertyChangedCallback(listener)

        dailyPredictionViewModel.loadPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS)
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(dailyPredictionViewModel.loading, BR._all)

        dailyPredictionViewModel.openRateDialog()
        Assert.assertEquals(
                Pair(DailyPredictionViewModel.Action.RATE_DAILY_PREDICTION,
                        dailyPredictionViewModel.dailyPredictionLiveData.value),
                dailyPredictionViewModel.actionsLiveData.value)
    }

    @Test
    fun test_daily_prediction_rate_success() {
        val dailyPredictionViewModel = initViewModel()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        dailyPredictionViewModel.loading.addOnPropertyChangedCallback(listener)

        dailyPredictionViewModel.loadPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS)
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(dailyPredictionViewModel.loading, BR._all)

        dailyPredictionViewModel.onAccuracySubmission(RATE_VALUE_SUCCESS)

        dailyPredictionViewModel.dailyPredictionLiveData.value?.let {
            Assert.assertEquals(RATE_VALUE_SUCCESS * DailyPredictionViewModel.ACCURACY_MULTIPLIER, it.accuracy())
        }

        Assert.assertEquals(ErrorPresentation(R.string.dailyPrediction_ratingComplete,
                0, "",
                ErrorPresentation.Companion.Type.SNACK_BAR),
                dailyPredictionViewModel.messagesLiveData.value)
    }

    @Test
    fun test_daily_prediction_rate_fail() {
        val dailyPredictionViewModel = initViewModel()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        dailyPredictionViewModel.loading.addOnPropertyChangedCallback(listener)

        dailyPredictionViewModel.loadPrediction(NATAL_DATE_ID, DAILY_PREDICTION_DATE_SUCCESS)
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(dailyPredictionViewModel.loading, BR._all)

        dailyPredictionViewModel.onAccuracySubmission(RATE_VALUE_FAIL)

        dailyPredictionViewModel.dailyPredictionLiveData.value?.let {
            Assert.assertEquals(DEFAULT_ACCURACY, it.accuracy())
        }

        Assert.assertEquals(ErrorPresentation(R.string.error_defaultTitle, R.string.error_general), dailyPredictionViewModel.messagesLiveData.value)
    }
}