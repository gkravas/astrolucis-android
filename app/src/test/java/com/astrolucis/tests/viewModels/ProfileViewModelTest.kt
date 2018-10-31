package com.astrolucis.tests.viewModels

import com.astrolucis.*
import com.astrolucis.core.BaseTest
import com.astrolucis.features.profile.ProfileViewModel
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.UserService
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
class ProfileViewModelTest: BaseTest() {

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

    private fun initViewModel(userService: UserService, natalDateService: NatalDateService): ProfileViewModel {
        return ProfileViewModel(RuntimeEnvironment.application, userService, natalDateService)
    }

    private fun initUserService(): UserService {
        return mock {
            on { resetPassword(VALID_PASSWORD) } doReturn Observable.just("")
            on { changeEmail(VALID_EMAIL) } doReturn Observable.just("")
            on { resetPassword(SERVER_ERROR_PASSWORD) } doReturn Observable.error(ErrorFactory.createServerError())
        }
    }

    private fun initNatalDateService(): NatalDateService {
        return mock {
            on { getAll() } doReturn Observable.just(UserFragment(EMPTY, VALID_EMAIL, EMPTY, null))
        }
    }

    @Test
    fun test_password_form_password_empty() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.EMPTY)
        profileViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), profileViewModel.passwordError.get())
    }

    @Test
    fun test_password_form_password_less_than_six() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.INVALID_SHORT_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), profileViewModel.passwordError.get())
    }

    @Test
    fun test_password_form_password_more_than_twenty() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.INVALID_LONG_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_validationError), profileViewModel.passwordError.get())
    }

    @Test
    fun test_password_form_password_valid() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        Assert.assertEquals("", profileViewModel.passwordError.get())
    }

    @Test
    fun test_password_form_password_repeat_empty() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordRepeatField.set(LoginViewModelTest.EMPTY)
        profileViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_password_emptyError), profileViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_form_password_repeat_not_equal_to_password() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        profileViewModel.passwordField.notifyChange()

        profileViewModel.passwordRepeatField.set(LoginViewModelTest.INVALID_SHORT_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_passwordRepeat_equalError), profileViewModel.passwordRepeatError.get())
    }

    @Test
    fun test_password_form_password_repeat_equal_to_password() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.passwordField.set(LoginViewModelTest.VALID_PASSWORD)
        profileViewModel.passwordField.notifyChange()

        profileViewModel.passwordRepeatField.set(LoginViewModelTest.VALID_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()
        Assert.assertEquals("", profileViewModel.passwordError.get())
    }

    @Test
    fun test_password_form_save() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        Assert.assertEquals("test form not loading password email password empty", false, profileViewModel.loadingPassword.get())

        profileViewModel.passwordField.set(INVALID_SHORT_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        profileViewModel.passwordRepeatField.set(INVALID_SHORT_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()
        profileViewModel.changePassword()
        Assert.assertEquals("test form not loading password invalid(less than 6)", false, profileViewModel.loadingPassword.get())

        profileViewModel.passwordField.set(VALID_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        profileViewModel.changePassword()
        Assert.assertEquals("test form not loading password repeat not equal to password", false, profileViewModel.loadingPassword.get())

        profileViewModel.passwordField.set(INVALID_LONG_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        profileViewModel.passwordRepeatField.set(INVALID_LONG_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()
        profileViewModel.changePassword()
        Assert.assertEquals("test form not loading password invalid(more than 20)", false, profileViewModel.loadingPassword.get())

        profileViewModel.emailField.set(EMPTY)
        profileViewModel.emailField.notifyChange()
        profileViewModel.passwordField.set(VALID_PASSWORD)
        profileViewModel.passwordField.notifyChange()
        profileViewModel.passwordRepeatField.set(VALID_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()
        profileViewModel.changePassword()
        Assert.assertEquals("test form not loading email empty", false, profileViewModel.loadingPassword.get())

        profileViewModel.emailField.set(VALID_EMAIL)
        profileViewModel.emailField.notifyChange()


        //test if it is not loading initially
        Assert.assertEquals(false, profileViewModel.loadingPassword.get())

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        profileViewModel.loadingPassword.addOnPropertyChangedCallback(listener)

        profileViewModel.changePassword()
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(profileViewModel.loadingPassword, BR._all)

        //test if it is not loading at the end of the goal
        Assert.assertEquals(false, profileViewModel.loadingPassword.get())
        Assert.assertEquals(ErrorPresentation(R.string.success_defaultTitle, R.string.profile_changePasswordSuccess_text),
                profileViewModel.messagesLiveData.value)
    }

    @Test
    fun test_password_form_server_error() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.initForm()

        profileViewModel.passwordField.set(SERVER_ERROR_PASSWORD)
        profileViewModel.passwordField.notifyChange()

        profileViewModel.passwordRepeatField.set(SERVER_ERROR_PASSWORD)
        profileViewModel.passwordRepeatField.notifyChange()

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        profileViewModel.loadingPassword.addOnPropertyChangedCallback(listener)

        profileViewModel.changePassword()
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(profileViewModel.loadingPassword, BR._all)

        Assert.assertEquals(null, profileViewModel.actionsLiveData.value)
        Assert.assertEquals("test server error",
                ErrorPresentation(R.string.error_defaultTitle, R.string.error_general),
                profileViewModel.messagesLiveData.value)
    }

    @Test
    fun test_email_form_email_empty() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.emailField.set(LoginViewModelTest.EMPTY)
        profileViewModel.emailField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_email_emptyError),
                profileViewModel.emailError.get())

        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        profileViewModel.loadingEmail.addOnPropertyChangedCallback(listener)
        profileViewModel.changeEmail()
        Mockito.verify(listener, Mockito.times(0)).onPropertyChanged(profileViewModel.loadingEmail, BR._all)
    }

    @Test
    fun test_email_form_email_invalid() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.emailField.set(LoginViewModelTest.INVALID_EMAIL)
        profileViewModel.emailField.notifyChange()
        Assert.assertEquals(resources.getText(R.string.login_email_validationError),
                profileViewModel.emailError.get())


        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        profileViewModel.loadingEmail.addOnPropertyChangedCallback(listener)
        profileViewModel.changeEmail()
        Mockito.verify(listener, Mockito.times(0)).onPropertyChanged(profileViewModel.loadingEmail, BR._all)
    }

    @Test
    fun test_email_form_email_valid() {
        val profileViewModel = initViewModel(initUserService(), initNatalDateService())

        profileViewModel.emailField.set(LoginViewModelTest.VALID_EMAIL)
        profileViewModel.emailField.notifyChange()
        Assert.assertEquals("", profileViewModel.emailError.get())

        //check if the loading flag was raised
        //2 times means that it changed to true(loading) and then to false(non loading)
        val listener = Mockito.mock(androidx.databinding.Observable.OnPropertyChangedCallback::class.java)
        profileViewModel.loadingEmail.addOnPropertyChangedCallback(listener)

        profileViewModel.changeEmail()
        Mockito.verify(listener, Mockito.times(2)).onPropertyChanged(profileViewModel.loadingEmail, BR._all)

        Assert.assertEquals(null, profileViewModel.actionsLiveData.value)
        Assert.assertEquals("test email changed",
                ErrorPresentation(R.string.success_defaultTitle, R.string.profile_changeEmailSuccess_text),
                profileViewModel.messagesLiveData.value)
    }
}
