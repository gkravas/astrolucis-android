package com.astrolucis.tests.viewModels

import com.astrolucis.*
import com.astrolucis.core.BaseTest
import com.astrolucis.features.natalDate.NatalDateViewModel
import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.fragment.UserFragment
import com.astrolucis.models.NatalType
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.type.natalDatetypeEnumType
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
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*


@RunWith(RobolectricTestRunner::class)
class NatalDateViewModelTest: BaseTest() {

    companion object {
        private const val EMPTY = ""
        private const val NATAL_DATE_ID: Long = 1
        private const val TEST_EMAIL: String = "test@astrolucis.gr"
        private const val VALID_LIVING_LOCATION = "Greece"
        private const val INVALID_LIVING_LOCATION = "invalid living location"
        private const val VALID_BIRTH_LOCATION = "Greece"
        private const val INVALID_BIRTH_LOCATION = "invalid birth location"
        private const val VALID_BIRTH_DATE = "16/08/1984"
        private const val VALID_BIRTH_TIME = "21:30"
        private const val INVALID_BIRTH_DATE = "17/08/1984"
        private const val INVALID_BIRTH_TIME = "22:30"
        private const val VALID_NAME = "me"
        private const val VALID_TYPE = "freeSpirit"
        private const val CLIENT_SIDE_FULL_DATE = "1984-08-16T21:30:00"
        private const val INVALID_CLIENT_SIDE_FULL_DATE = "1984-08-17T22:30:00"
        private const val SERVER_SIDE_FULL_DATE = "Thu Aug 16 1984 21:30:00 GMT+0200 (CEST)"
    }

    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    @After
    fun after(){
        StandAloneContext.closeKoin()
    }

    private fun initUserService(): UserService {
        return mock {
            on { updateLivingLocation(VALID_LIVING_LOCATION) } doReturn Observable.just(createValidUser())
            on { updateLivingLocation(INVALID_LIVING_LOCATION) } doReturn Observable.error(ErrorFactory.createLivingLocationError())
        }
    }

    private fun createValidUser(): UserFragment {
        val natalDateFragment = NatalDateFragment(EMPTY, NATAL_DATE_ID, VALID_NAME, SERVER_SIDE_FULL_DATE,
                VALID_BIRTH_LOCATION, true, natalDatetypeEnumType.freeSpirit)
        val natalDate = UserFragment.NatalDate(EMPTY, UserFragment.NatalDate.Fragments(natalDateFragment))

        return UserFragment(EMPTY, TEST_EMAIL, VALID_LIVING_LOCATION, Arrays.asList(natalDate))
    }

    private fun initNatalDateService(returnNatalDate: Boolean): NatalDateService {
        val natalDateFragment = NatalDateFragment(EMPTY, 1, VALID_NAME, SERVER_SIDE_FULL_DATE,
                VALID_LIVING_LOCATION, true, natalDatetypeEnumType.freeSpirit)
        val observable = if (returnNatalDate) {
            val natalDate = UserFragment.NatalDate("", UserFragment.NatalDate.Fragments(natalDateFragment))
            Observable.just(UserFragment(EMPTY, TEST_EMAIL, VALID_LIVING_LOCATION, Arrays.asList(natalDate)))
        } else {
            Observable.just(UserFragment(EMPTY, TEST_EMAIL, EMPTY, null))
        }
        return mock {
            on { getAll() } doReturn observable
            on { createNatalDateMutation(CLIENT_SIDE_FULL_DATE, VALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.just(natalDateFragment)
            on { updateNatalDateMutation(1, CLIENT_SIDE_FULL_DATE, VALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.just(natalDateFragment)
            on { createNatalDateMutation(CLIENT_SIDE_FULL_DATE, INVALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.error(ErrorFactory.createBirthLocationError())
            on { updateNatalDateMutation(1, CLIENT_SIDE_FULL_DATE, INVALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.error(ErrorFactory.createBirthLocationError())
            on { createNatalDateMutation(INVALID_CLIENT_SIDE_FULL_DATE, VALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.error(ErrorFactory.createBirthDateError())
            on { updateNatalDateMutation(1, INVALID_CLIENT_SIDE_FULL_DATE, VALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.error(ErrorFactory.createBirthDateError())

            on { createNatalDateMutation(INVALID_CLIENT_SIDE_FULL_DATE, INVALID_BIRTH_LOCATION, VALID_NAME, true, VALID_TYPE) } doReturn Observable.error(ErrorFactory.createServerError())
        }
    }

    private fun initViewModel(userService: UserService, natalDateService: NatalDateService, preferences: Preferences): NatalDateViewModel {
        return NatalDateViewModel(RuntimeEnvironment.application, userService, natalDateService, preferences)
    }

    private fun checkFormNotAbleToSave(natalDateViewModel: NatalDateViewModel) {
        Assert.assertEquals("test form not able to save", false, natalDateViewModel.loading.get())
    }

    //TEST FORM VALIDATOR
    @Test
    fun test_form_validator_living_location_empty() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(EMPTY)
        natalDateViewModel.livingLocationField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.natalDate_livingLocation_emptyError),
                natalDateViewModel.livingLocationError.get())
    }

    @Test
    fun test_form_validator_birth_location_empty() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.birthLocationField.set(EMPTY)
        natalDateViewModel.birthLocationField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.natalDate_birthLocation_emptyError),
                natalDateViewModel.birthLocationError.get())
    }

    @Test
    fun test_form_validator_birth_date_empty() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.birthDateField.set(EMPTY)
        natalDateViewModel.birthDateField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.natalDate_birthDate_emptyError),
                natalDateViewModel.birthDateError.get())
    }

    @Test
    fun test_form_validator_birth_time_empty() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.birthTimeField.set(EMPTY)
        natalDateViewModel.birthTimeField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.natalDate_birthTime_emptyError),
                natalDateViewModel.birthTimeError.get())
    }

    //FORM SUBMISSION
    @Test
    fun test_form_cannot_submitted_empty_living_location() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(EMPTY)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.save()

        checkFormNotAbleToSave(natalDateViewModel)
    }

    @Test
    fun test_form_cannot_submitted_empty_birth_location() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(EMPTY)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.save()

        checkFormNotAbleToSave(natalDateViewModel)
    }

    @Test
    fun test_form_cannot_submitted_empty_birth_date() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(EMPTY)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.save()

        checkFormNotAbleToSave(natalDateViewModel)
    }

    @Test
    fun test_form_cannot_submitted_empty_birth_time() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(EMPTY)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.save()

        checkFormNotAbleToSave(natalDateViewModel)
    }

    @Test
    fun test_form_can_be_submitted() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        //test if it is not loading initially
        Assert.assertEquals(false, natalDateViewModel.loading.get())

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = mock(android.databinding.Observable.OnPropertyChangedCallback::class.java)
        natalDateViewModel.loading.addOnPropertyChangedCallback(listener)

        natalDateViewModel.save()
        verify(listener, times(2)).onPropertyChanged(natalDateViewModel.loading, BR._all)

        //test if it is not loading at the end of the goal
        Assert.assertEquals(false, natalDateViewModel.loading.get())
        Assert.assertEquals(VALID_LIVING_LOCATION, natalDateViewModel.livingLocationField.get())
        Assert.assertEquals(VALID_BIRTH_LOCATION, natalDateViewModel.birthLocationField.get())
        Assert.assertEquals(VALID_BIRTH_DATE, natalDateViewModel.birthDateField.get())
        Assert.assertEquals(VALID_BIRTH_TIME, natalDateViewModel.birthTimeField.get())
    }

    //FORM INITIALIZATION
    @Test
    fun test_form_initialization_user_has_no_natal_date() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        Assert.assertEquals(resources.getString(R.string.natalDate_defaultCountry),
                natalDateViewModel.livingLocationField.get())
        Assert.assertEquals(EMPTY, natalDateViewModel.birthLocationField.get())
        Assert.assertEquals(EMPTY, natalDateViewModel.birthDateField.get())
        Assert.assertEquals(resources.getString(R.string.natalDate_defaultTime),
                natalDateViewModel.birthTimeField.get())
    }

    @Test
    fun test_form_initialization_user_has_natal_date() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING, createValidUser()))

        Assert.assertEquals(VALID_LIVING_LOCATION, natalDateViewModel.livingLocationField.get())
        Assert.assertEquals(VALID_BIRTH_LOCATION, natalDateViewModel.birthLocationField.get())
        Assert.assertEquals(VALID_BIRTH_DATE, natalDateViewModel.birthDateField.get())
        Assert.assertEquals(VALID_BIRTH_TIME, natalDateViewModel.birthTimeField.get())
        Assert.assertEquals(resources.getText(R.string.natalType_freeSpirit), natalDateViewModel.typeField.get())
    }

    //ERROR HANDLING
    @Test
    fun test_error_handling_create_natal_date_birth_location_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(INVALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.typeField.set(resources.getText(NatalType.findBy(VALID_TYPE)?.resourceId!!))
        natalDateViewModel.typeField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user create natal date birth location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthLocation),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_update_natal_date_birth_location_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING, createValidUser()))

        natalDateViewModel.birthLocationField.set(INVALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user update natal date birth location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthLocation),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_create_natal_date_living_location_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(INVALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(VALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(VALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.typeField.set(resources.getText(NatalType.findBy(VALID_TYPE)?.resourceId!!))
        natalDateViewModel.typeField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user create natal date living location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidLivingLocation),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_update_natal_date_living_location_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING, createValidUser()))

        natalDateViewModel.livingLocationField.set(INVALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user update natal date living location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidLivingLocation),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_create_natal_date_birth_date_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(VALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(INVALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(INVALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.typeField.set(resources.getText(NatalType.findBy(VALID_TYPE)?.resourceId!!))
        natalDateViewModel.typeField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user create natal date living location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthDate),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_update_natal_date_birth_date_should_fail() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(true),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING, createValidUser()))

        natalDateViewModel.birthDateField.set(INVALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(INVALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test user update natal date living location should fail",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidBirthDate),
                natalDateViewModel.messagesLiveData.value)
    }

    @Test
    fun test_error_handling_server_error() {
        val natalDateViewModel = initViewModel(initUserService(),
                initNatalDateService(false),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        natalDateViewModel.livingLocationField.set(VALID_LIVING_LOCATION)
        natalDateViewModel.livingLocationField.notifyChange()

        natalDateViewModel.birthLocationField.set(INVALID_BIRTH_LOCATION)
        natalDateViewModel.birthLocationField.notifyChange()

        natalDateViewModel.birthDateField.set(INVALID_BIRTH_DATE)
        natalDateViewModel.birthDateField.notifyChange()

        natalDateViewModel.birthTimeField.set(INVALID_BIRTH_TIME)
        natalDateViewModel.birthTimeField.notifyChange()

        natalDateViewModel.typeField.set(resources.getText(NatalType.findBy(VALID_TYPE)?.resourceId!!))
        natalDateViewModel.typeField.notifyChange()

        natalDateViewModel.save()

        Assert.assertEquals("test handling server error",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_general),
                natalDateViewModel.messagesLiveData.value)
    }
}