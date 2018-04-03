package com.astrolucis.features.login

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.databinding.Observable
import android.databinding.Observable.OnPropertyChangedCallback
import android.databinding.ObservableField
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.repsonses.LoginResponse
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.validators.EmailValidator
import com.astrolucis.utils.validators.EmptyValidator
import com.astrolucis.utils.validators.EqualFieldsValidator
import com.astrolucis.utils.validators.RangeValidator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LoginViewModel: BaseViewModel {

    companion object {
        const val PASSWORD_MIN_LENGTH: Int = 6
        const val PASSWORD_MAX_LENGTH: Int = 20
    }

    enum class ViewState {
        LOGIN,
        REGISTER
    }

    enum class Action {
        GO_TO_HOME
    }

    val viewState: ObservableField<ViewState> = ObservableField()

    val emailField: ObservableField<CharSequence> = ObservableField("")
    val passwordField: ObservableField<CharSequence> = ObservableField("")
    val passwordRepeatField: ObservableField<CharSequence> = ObservableField("")

    val emailError: ObservableField<CharSequence> = ObservableField("")
    val passwordError: ObservableField<CharSequence> = ObservableField("")
    val passwordRepeatError: ObservableField<CharSequence> = ObservableField("")

    val loading: ObservableField<Boolean> = ObservableField(false)
    val actionButtonText: ObservableField<CharSequence> = ObservableField("")
    val toggleStateText: ObservableField<CharSequence> = ObservableField("")

    private val userService: UserService
    val preferences: Preferences

    private val disposables = CompositeDisposable()

    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()

    constructor(application: Application, userService: UserService, preferences: Preferences) : super(application) {

        this.userService = userService
        this.preferences = preferences

        this.viewState.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val actionText: Int = if (viewState.get() == ViewState.LOGIN) {
                    R.string.login_loginButton_text
                } else {
                    R.string.login_registerButton_text
                }
                actionButtonText.set(getApplication<App>().resources.getText(actionText))

                val toggleText: Int = if (viewState.get() == ViewState.LOGIN) {
                    R.string.login_registerHint
                } else {
                    R.string.login_loginHint
                }
                toggleStateText.set(getApplication<App>().resources.getText(toggleText))
            }
        })

        this.emailField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validateEmail()
            }
        })

        this.passwordField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validatePassword()
            }
        })

        this.passwordRepeatField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validatePasswordRepeat()
            }
        })

        this.viewState.set(ViewState.LOGIN)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    private fun validateEmail(): Boolean {
        val application = getApplication<App>()
        return EmptyValidator(emailField, emailError,
                application.getText(R.string.login_email_emptyError)).isValid
                &&
                EmailValidator(emailField, emailError,
                application.getText(R.string.login_email_validationError)).isValid
    }

    private fun validatePassword(): Boolean {
        val application = getApplication<App>()
        return EmptyValidator(passwordField, passwordError,
                application.getText(R.string.login_password_emptyError)).isValid
                &&
                RangeValidator(passwordField, passwordError,
                        application.getText(R.string.login_password_validationError),
                        PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH).isValid
    }

    private fun validatePasswordRepeat(): Boolean {
        val application = getApplication<App>()
        return EmptyValidator(passwordRepeatField, passwordRepeatError,
                application.getText(R.string.login_password_emptyError)).isValid
                &&
                EqualFieldsValidator(passwordRepeatField, passwordField, passwordRepeatError,
                    application.getText(R.string.login_passwordRepeat_equalError)).isValid
    }

    fun login() {
        if (!validateEmail() || !validatePassword()) {
            return
        }

        disposables.add(userService.login(emailField.get().toString(), passwordField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        { response -> handleLoginSuccess(response) },
                        { handleError(it) }
                )
        )
    }

    fun fbLogin(fbToken: String?) {
        if (fbToken.isNullOrEmpty()) {
            return
        }
        disposables.add(userService.fbLogin(fbToken!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        { response -> handleLoginSuccess(response) },
                        { handleError(it) }
                )
        )
    }

    fun register() {
        if (!validateEmail() || !validatePassword() || !validatePasswordRepeat()) {
            return
        }

        disposables.add(userService.register(emailField.get().toString(), passwordField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        { login() },
                        { handleError(it) }
                )
        )
    }

    fun sendForgotPassword() {
        if (!validateEmail()) {
            return
        }
        disposables.add(userService.sendResetEmail(emailField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.set(true) })
                .subscribe(
                        {
                            messagesLiveData.value = ErrorPresentation(R.string.error_defaultTitle, R.string.login_remindPassword)
                            loading.set(false)
                        },
                        { handleError(it) }
                )
        )
    }

    fun toggleViewState() {
        this.viewState.set(if (this.viewState.get() == ViewState.LOGIN) ViewState.REGISTER else ViewState.LOGIN)
    }

    fun loginRegister() {
        if (viewState.get() == ViewState.LOGIN) {
            login()
        } else {
            register()
        }
    }

    private fun handleLoginSuccess(loginResponse: LoginResponse) {
        preferences.token = loginResponse.token
        actionsLiveData.value = Action.GO_TO_HOME
    }

    private fun handleError(throwable: Throwable) {
        messagesLiveData.value = ErrorHandler.handleLoginRegister(throwable)
        loading.set(false)
    }
}
