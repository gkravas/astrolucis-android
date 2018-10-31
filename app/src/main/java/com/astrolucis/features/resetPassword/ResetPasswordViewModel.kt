package com.astrolucis.features.resetPassword

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import android.text.TextUtils
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.JWTUtils
import com.astrolucis.utils.validators.EmailValidator
import com.astrolucis.utils.validators.EmptyValidator
import com.astrolucis.utils.validators.EqualFieldsValidator
import com.astrolucis.utils.validators.RangeValidator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ResetPasswordViewModel: BaseViewModel {

    companion object {
        const val PASSWORD_MIN_LENGTH: Int = 6
        const val PASSWORD_MAX_LENGTH: Int = 20
        const val EXPIRED_DIALOG_ID: String = "expiredDialogId"
    }

    enum class Action {
        GO_TO_HOME
    }

    val emailField: ObservableField<CharSequence> = ObservableField("")
    val passwordField: ObservableField<CharSequence> = ObservableField("")
    val passwordRepeatField: ObservableField<CharSequence> = ObservableField("")

    val emailError: ObservableField<CharSequence> = ObservableField("")
    val passwordError: ObservableField<CharSequence> = ObservableField("")
    val passwordRepeatError: ObservableField<CharSequence> = ObservableField("")

    private val userService: UserService
    private val natalDateService: NatalDateService
    val preferences: Preferences

    private val disposables = CompositeDisposable()

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()

    constructor(application: Application, userService: UserService,
                natalDateService: NatalDateService, preferences: Preferences) : super(application) {

        this.userService = userService
        this.natalDateService = natalDateService
        this.preferences = preferences

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

        loading.value = false
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

    fun changePassword() {
        if (!validateEmail() || !validatePassword() || !validatePasswordRepeat()) {
            return
        }
        if (!JWTUtils.isLoggedIn(preferences.token)) {
            messagesLiveData.value = ErrorPresentation(R.string.error_general, R.string.error_resetPassword_tokenExpired)
            return
        }
        disposables.add(userService.resetPassword(passwordField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.value = true })
                .subscribe(
                        {
                            loading.value = false
                            preferences.reset()
                            actionsLiveData.value = Action.GO_TO_HOME
                        },
                        { handleError(it) }
                )
        )
    }

    fun reset() {
        preferences.reset()
    }

    fun initForm() {
        if (TextUtils.isEmpty(preferences.token)) {
            actionsLiveData.value = Action.GO_TO_HOME
            return
        }
        if (!JWTUtils.isLoggedIn(preferences.token)) {
            messagesLiveData.value = ErrorPresentation(R.string.error_defaultTitle,
                    R.string.error_resetPassword_tokenExpired,
                    EXPIRED_DIALOG_ID)
            return
        }

        disposables.add(natalDateService.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.value = true })
                .subscribe(
                        { me ->
                            emailField.set(me.email())
                            loading.value = false
                        },
                        { throwable ->
                            messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                            loading.value = false
                        }
                ))
    }

    private fun handleError(throwable: Throwable) {
        messagesLiveData.value = ErrorHandler.handleLoginRegister(throwable)
        loading.value = false
    }

    override fun onDialogAction(id: String, positive: Boolean) {
        when (id) {
            EXPIRED_DIALOG_ID -> actionsLiveData.value = Action.GO_TO_HOME
        }
    }
}
