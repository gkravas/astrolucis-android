package com.astrolucis.tests.viewModels

import com.astrolucis.*
import com.astrolucis.core.BaseTest
import com.astrolucis.features.resetPassword.ResetPasswordViewModel
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.tests.utils.Constants.Companion.EXPIRED_JWT
import com.astrolucis.tests.utils.Constants.Companion.NON_EXPIRING_JWT
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

@RunWith(RobolectricTestRunner::class)
class ResetPasswordViewModelTest: BaseTest() {

    companion object {
        private const val EMPTY = ""

        const val VALID_EMAIL: String = "test@astrolucis.gr"
        const val VALID_PASSWORD: String = "123456"
        const val SERVER_ERROR_PASSWORD: String = "serverError"

        const val INVALID_SHORT_PASSWORD: String = "12345"
        const val INVALID_LONG_PASSWORD: String = "012345678901234567891"

    }

    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    @After
    fun after() {
        StandAloneContext.closeKoin()
    }

    private fun initViewModel(userService: UserService, natalDateService: NatalDateService,
                              preferences: Preferences): ResetPasswordViewModel {
        return ResetPasswordViewModel(RuntimeEnvironment.application, userService, natalDateService, preferences)
    }

    private fun initUserService(): UserService {
        return mock {
            on { resetPassword(VALID_PASSWORD) } doReturn Observable.just("")
            on { resetPassword(SERVER_ERROR_PASSWORD) } doReturn Observable.error(ErrorFactory.createServerError())
        }
    }

    private fun initNatalDateService(): NatalDateService {
        return mock {
            on { getAll() } doReturn Observable.just(UserFragment(EMPTY, VALID_EMAIL, EMPTY, null))
        }
    }

    @Test
    fun test_password_empty() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.EMPTY)
        resetPasswordViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), resetPasswordViewModel.passwordError.get())
    }

    @Test
    fun test_password_less_than_six() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.INVALID_SHORT_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), resetPasswordViewModel.passwordError.get())
    }

    @Test
    fun test_password_more_than_twenty() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.INVALID_LONG_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), resetPasswordViewModel.passwordError.get())
    }

    @Test
    fun test_password_valid() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        Assert.assertEquals("", resetPasswordViewModel.passwordError.get())
    }

    @Test
    fun test_password_repeat_empty() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordRepeatField.set(LoginViewModelTest.EMPTY)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), resetPasswordViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_repeat_not_equal_to_password() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()

        resetPasswordViewModel.passwordRepeatField.set(LoginViewModelTest.INVALID_SHORT_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_passwordRepeat_equalError), resetPasswordViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_repeat_equal_to_password() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()

        resetPasswordViewModel.passwordRepeatField.set(LoginViewModelTest.VALID_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals("", resetPasswordViewModel.passwordError.get())
    }

    @Test
    fun test_register_form() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        Assert.assertEquals("test form not loading password email password empty", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.passwordField.set(INVALID_SHORT_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        resetPasswordViewModel.passwordRepeatField.set(INVALID_SHORT_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        resetPasswordViewModel.changePassword()
        Assert.assertEquals("test form not loading password invalid(less than 6)", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.passwordField.set(VALID_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        resetPasswordViewModel.changePassword()
        Assert.assertEquals("test form not loading password repeat not equal to password", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.passwordField.set(INVALID_LONG_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        resetPasswordViewModel.passwordRepeatField.set(INVALID_LONG_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        resetPasswordViewModel.changePassword()
        Assert.assertEquals("test form not loading password invalid(more than 20)", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.emailField.set(EMPTY)
        resetPasswordViewModel.emailField.notifyChange()
        resetPasswordViewModel.passwordField.set(VALID_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()
        resetPasswordViewModel.passwordRepeatField.set(VALID_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()
        resetPasswordViewModel.changePassword()
        Assert.assertEquals("test form not loading email empty", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.emailField.set(LoginViewModelTest.INVALID_EMAIL)
        resetPasswordViewModel.emailField.notifyChange()
        resetPasswordViewModel.changePassword()
        Assert.assertEquals("test form not loading email invalid", false, resetPasswordViewModel.loading.get())

        resetPasswordViewModel.emailField.set(VALID_EMAIL)
        resetPasswordViewModel.emailField.notifyChange()


        //test if it is not loading initially
        Assert.assertEquals(false, resetPasswordViewModel.loading.get())

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        resetPasswordViewModel.loading.addOnPropertyChangedCallback(listener)

        resetPasswordViewModel.changePassword()
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(resetPasswordViewModel.loading, BR._all)

        //test if it is not loading at the end of the goal
        Assert.assertEquals(false, resetPasswordViewModel.loading.get())
        Assert.assertEquals(ResetPasswordViewModel.Action.GO_TO_HOME, resetPasswordViewModel.actionsLiveData.value)
    }

    @Test
    fun test_server_error() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(NON_EXPIRING_JWT))

        resetPasswordViewModel.initForm()

        resetPasswordViewModel.passwordField.set(SERVER_ERROR_PASSWORD)
        resetPasswordViewModel.passwordField.notifyChange()

        resetPasswordViewModel.passwordRepeatField.set(SERVER_ERROR_PASSWORD)
        resetPasswordViewModel.passwordRepeatField.notifyChange()

        resetPasswordViewModel.changePassword()

        Assert.assertEquals(null, resetPasswordViewModel.actionsLiveData.value)
        Assert.assertEquals("test server error",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_general),
                resetPasswordViewModel.messagesLiveData.value)
    }

    @Test
    fun test_expired_token() {
        val resetPasswordViewModel = initViewModel(initUserService(), initNatalDateService(),
                initPreferences(EXPIRED_JWT))

        resetPasswordViewModel.initForm()

        Assert.assertEquals(null, resetPasswordViewModel.actionsLiveData.value)
        Assert.assertEquals("test server error",
                ErrorPresentation(R.string.error_defaultTitle,
                        R.string.error_resetPassword_tokenExpired,
                        ResetPasswordViewModel.EXPIRED_DIALOG_ID),
                resetPasswordViewModel.messagesLiveData.value)
    }
}
