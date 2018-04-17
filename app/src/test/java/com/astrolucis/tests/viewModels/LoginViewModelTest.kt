package com.astrolucis.tests.viewModels

import com.astrolucis.core.BaseTest
import com.astrolucis.utils.ErrorFactory
import com.astrolucis.R
import com.astrolucis.utils.TrampolineSchedulerRule
import com.astrolucis.features.login.LoginViewModel
import com.astrolucis.models.User
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.services.repsonses.LoginResponse
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.dialogs.AlertDialog
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LoginViewModelTest: BaseTest() {

    companion object {
        const val EMPTY: String = ""

        const val VALID_EMAIL: String = "test@astrolucis.gr"
        const val VALID_PASSWORD: String = "123456"

        const val INVALID_EMAIL: String = "test.astrolucis.gr"
        const val INVALID_SHORT_PASSWORD: String = "12345"
        const val INVALID_LONG_PASSWORD: String = "012345678901234567891"

        const val NO_USER_EMAIL: String = "noUserFound@astrolucis.gr"
        const val NO_USER_PASSWORD: String = "noUserFound"

        const val DUPLICATE_USER_EMAIL: String = "duplicateUser@astrolucis.gr"
        const val DUPLICATE_USER_PASSWORD: String = "duplicateUser"

        const val SERVER_ERROR_EMAIL: String = "serverError@astrolucis.gr"
        const val SERVER_ERROR_PASSWORD: String = "serverError"
    }

    @get:Rule
    public val testSchedulerRule: TrampolineSchedulerRule = TrampolineSchedulerRule()

    @After
    fun after(){
        StandAloneContext.closeKoin()
    }

    private fun initViewModel(userService: UserService, preferences: Preferences): LoginViewModel {
        return LoginViewModel(RuntimeEnvironment.application, userService, preferences)
    }

    private fun initUserService(): UserService {
        return mock {
            on { login(VALID_EMAIL, VALID_PASSWORD) } doReturn Observable.just(LoginResponse(User(1, VALID_EMAIL, arrayOf()), ""))
            on { register(VALID_EMAIL, VALID_PASSWORD) } doReturn Observable.just("")
            on { sendResetEmail(VALID_EMAIL) } doReturn Observable.just("")
            on { login(NO_USER_EMAIL, NO_USER_PASSWORD) } doReturn Observable.error(ErrorFactory.createInvalidCredentialsError())
            on { login(EMPTY, VALID_PASSWORD) } doReturn Observable.error(ErrorFactory.createEmailEmptyError())
            on { login(SERVER_ERROR_EMAIL, SERVER_ERROR_PASSWORD) } doReturn Observable.error(ErrorFactory.createServerError())
            on { register(SERVER_ERROR_EMAIL, SERVER_ERROR_PASSWORD) } doReturn Observable.error(ErrorFactory.createServerError())
            on { register(DUPLICATE_USER_EMAIL, DUPLICATE_USER_PASSWORD) } doReturn Observable.error(ErrorFactory.createDuplicateUserError())
        }
    }

    @Test
    fun test_email_empty() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.emailField.set(EMPTY)
        loginViewModel.emailField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_email_emptyError),
                loginViewModel.emailError.get())
    }

    @Test
    fun test_email_invalid() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.emailField.set(INVALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_email_validationError),
                loginViewModel.emailError.get())
    }

    @Test
    fun test_email_valid() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        Assert.assertEquals("", loginViewModel.emailError.get())
    }

    @Test
    fun test_password_empty() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(EMPTY)
        loginViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), loginViewModel.passwordError.get())
    }

    @Test
    fun test_password_less_than_six() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(INVALID_SHORT_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), loginViewModel.passwordError.get())
    }

    @Test
    fun test_password_more_than_twenty() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(INVALID_LONG_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), loginViewModel.passwordError.get())
    }

    @Test
    fun test_password_valid() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        Assert.assertEquals("", loginViewModel.passwordError.get())
    }

    @Test
    fun test_password_repeat_empty() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordRepeatField.set(EMPTY)
        loginViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), loginViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_repeat_not_equal_to_password() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.passwordRepeatField.set(INVALID_SHORT_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_passwordRepeat_equalError), loginViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_repeat_equal_to_password() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.passwordRepeatField.set(VALID_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals("", loginViewModel.passwordError.get())
    }

    @Test
    fun test_login_form() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.LOGIN)
        loginViewModel.viewState.notifyChange()

        Assert.assertEquals("test login form not loading password email password empty", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(INVALID_SHORT_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.login()
        Assert.assertEquals("test login form not loading password invalid(less than 6)", false, loginViewModel.loading.get())

        loginViewModel.passwordField.set(INVALID_LONG_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.login()
        Assert.assertEquals("test login form not loading password invalid(more than 20)", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(EMPTY)
        loginViewModel.emailField.notifyChange()
        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.login()
        Assert.assertEquals("test login form not loading email empty", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(INVALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        loginViewModel.login()
        Assert.assertEquals("test login form not loading email invalid", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        loginViewModel.login()
        Assert.assertEquals("test login form loading", true, loginViewModel.loading.get())
    }

    @Test
    fun test_register_form() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.REGISTER)
        loginViewModel.viewState.notifyChange()

        Assert.assertEquals("test login form not loading password email password empty", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(INVALID_SHORT_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.passwordRepeatField.set(INVALID_SHORT_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form not loading password invalid(less than 6)", false, loginViewModel.loading.get())

        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form not loading password repeat not equal to password", false, loginViewModel.loading.get())

        loginViewModel.passwordField.set(INVALID_LONG_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.passwordRepeatField.set(INVALID_LONG_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form not loading password invalid(more than 20)", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(EMPTY)
        loginViewModel.emailField.notifyChange()
        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()
        loginViewModel.passwordRepeatField.set(VALID_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form not loading email empty", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(INVALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form not loading email invalid", false, loginViewModel.loading.get())

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()
        loginViewModel.register()
        Assert.assertEquals("test register form loading", true, loginViewModel.loading.get())
    }

    @Test
    fun test_toggle_form_state() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        Assert.assertEquals("test form is in register mode",
                resources.getText(R.string.login_registerHint), loginViewModel.toggleStateText.get())

        loginViewModel.toggleViewState()

        Assert.assertEquals("test form is in login mode",
                resources.getText(R.string.login_loginHint), loginViewModel.toggleStateText.get())
    }

    @Test
    fun test_password_reminder() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.emailField.set(VALID_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.sendForgotPassword()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test forgot password message appeared after action",
                ErrorPresentation(R.string.error_defaultTitle, R.string.login_remindPassword),
                loginViewModel.messagesLiveData.value)
    }

    @Test
    fun test_login_server_error() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.LOGIN)
        loginViewModel.viewState.notifyChange()

        loginViewModel.emailField.set(SERVER_ERROR_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(SERVER_ERROR_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.login()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test login server error",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_general),
                loginViewModel.messagesLiveData.value)
    }

    @Test
    fun test_login_wrong_credentials() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.LOGIN)
        loginViewModel.viewState.notifyChange()

        loginViewModel.emailField.set(NO_USER_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(NO_USER_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.login()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test user wrong credentials",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_invalidCredential, AlertDialog.LOGOUT_DIALOG_ID),
                loginViewModel.messagesLiveData.value)
    }

    @Test
    fun test_register_server_error() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.REGISTER)
        loginViewModel.viewState.notifyChange()

        loginViewModel.emailField.set(SERVER_ERROR_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(SERVER_ERROR_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.passwordRepeatField.set(SERVER_ERROR_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()

        loginViewModel.register()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test register server error",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_general),
                loginViewModel.messagesLiveData.value)
    }

    @Test
    fun test_register_user_exists() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.REGISTER)
        loginViewModel.viewState.notifyChange()

        loginViewModel.emailField.set(DUPLICATE_USER_EMAIL)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(DUPLICATE_USER_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.passwordRepeatField.set(DUPLICATE_USER_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()

        loginViewModel.register()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test user duplicate registration",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_notUniqueEmail),
                loginViewModel.messagesLiveData.value)
    }

    @Test
    fun test_register_empty_email_should_fail() {
        val loginViewModel = initViewModel(initUserService(),
                initPreferences(com.astrolucis.services.interfaces.Preferences.EMPTY_STRING))

        loginViewModel.viewState.set(LoginViewModel.ViewState.REGISTER)
        loginViewModel.viewState.notifyChange()

        loginViewModel.emailField.set(EMPTY)
        loginViewModel.emailField.notifyChange()

        loginViewModel.passwordField.set(VALID_PASSWORD)
        loginViewModel.passwordField.notifyChange()

        loginViewModel.passwordRepeatField.set(VALID_PASSWORD)
        loginViewModel.passwordRepeatField.notifyChange()

        loginViewModel.register()

        Assert.assertEquals(null, loginViewModel.actionsLiveData.value)
        Assert.assertEquals("test user register empty email, message is",
                null,
                loginViewModel.messagesLiveData.value)
    }
}