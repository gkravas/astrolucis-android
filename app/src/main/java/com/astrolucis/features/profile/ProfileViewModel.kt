package com.astrolucis.features.profile

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.databinding.Observable
import android.databinding.ObservableField
import android.text.TextUtils
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.JWTUtils
import com.astrolucis.utils.dialogs.AlertDialog.Companion.LOGOUT_DIALOG_ID
import com.astrolucis.utils.validators.EmailValidator
import com.astrolucis.utils.validators.EmptyValidator
import com.astrolucis.utils.validators.EqualFieldsValidator
import com.astrolucis.utils.validators.RangeValidator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ProfileViewModel: BaseViewModel {

    companion object {
        const val PASSWORD_MIN_LENGTH: Int = 6
        const val PASSWORD_MAX_LENGTH: Int = 20
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

    private val disposables = CompositeDisposable()

    val loadingEmail: MutableLiveData<Boolean> = MutableLiveData()
    val loadingPassword: MutableLiveData<Boolean> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()

    constructor(application: Application, userService: UserService,
                natalDateService: NatalDateService) : super(application) {

        this.userService = userService
        this.natalDateService = natalDateService

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

        loadingEmail.value = false
        loadingPassword.value = false
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

    fun changeEmail() {
        if (!validateEmail()) {
            return
        }
        disposables.add(userService.changeEmail(emailField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loadingEmail.value = true })
                .subscribe(
                        {
                            loadingEmail.value = false
                            messagesLiveData.value = ErrorPresentation(R.string.success_defaultTitle,
                                    R.string.profile_changeEmailSuccess_text)
                        },
                        { handleError(it) }
                )
        )
    }

    fun changePassword() {
        if (!validatePassword() || !validatePasswordRepeat()) {
            return
        }
        disposables.add(userService.resetPassword(passwordField.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loadingPassword.value = true })
                .subscribe(
                        {
                            loadingPassword.value = false
                            messagesLiveData.value = ErrorPresentation(R.string.success_defaultTitle,
                                        R.string.profile_changePasswordSuccess_text)
                        },
                        { handleError(it) }
                )
        )
    }

    fun initForm() {
        disposables.add(natalDateService.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    loadingEmail.value = true
                    loadingPassword.value = true
                })
                .subscribe(
                        { me ->
                            emailField.set(me.email())
                            loadingEmail.value = false
                            loadingPassword.value = false
                        },
                        { handleError(it) }
                ))
    }

    private fun handleError(throwable: Throwable) {
        messagesLiveData.value = ErrorHandler.handleLoginRegister(throwable)
        loadingEmail.value = false
        loadingPassword.value = false
    }

    override fun onDialogAction(id: String, positive: Boolean) {
        when (id) {
            LOGOUT_DIALOG_ID -> actionsLiveData.value = Action.GO_TO_HOME
        }
    }
}
