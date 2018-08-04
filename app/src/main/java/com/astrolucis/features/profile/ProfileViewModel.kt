package com.astrolucis.features.profile

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.databinding.Observable
import android.databinding.ObservableField
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.features.home.HomeViewModel
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.dialogs.AlertDialog.Companion.LOGOUT_DIALOG_ID
import com.astrolucis.utils.validators.EmailValidator
import com.astrolucis.utils.validators.EmptyValidator
import com.astrolucis.utils.validators.EqualFieldsValidator
import com.astrolucis.utils.validators.RangeValidator
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProfileViewModel: BaseViewModel {

    companion object {
        const val PASSWORD_MIN_LENGTH: Int = 6
        const val PASSWORD_MAX_LENGTH: Int = 20

        const val DAILY: String = "daily"
        const val DAILY_EL: String = "daily_el"
        const val DAILY_EN: String = "daily_en"
        const val GREEK_LANGUAGE: String = "el"
        const val OS: String = "android"
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
    private val preferences: Preferences
    private val natalDateService: NatalDateService

    private val disposables = CompositeDisposable()

    val loadingEmail: MutableLiveData<Boolean> = MutableLiveData()
    val loadingPassword: MutableLiveData<Boolean> = MutableLiveData()
    val dailyNotifications: MutableLiveData<Boolean> = MutableLiveData()
    val personalNotifications: MutableLiveData<Boolean> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()

    constructor(application: Application, userService: UserService, preferences: Preferences,
                natalDateService: NatalDateService) : super(application) {

        this.userService = userService
        this.preferences = preferences
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
        dailyNotifications.value = preferences.dailyNotifications
        personalNotifications.value = preferences.personalNotifications
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun registerDailyNotifications() {
        preferences.dailyNotifications = true
        val language = Locale.getDefault().language
        FirebaseMessaging.getInstance().subscribeToTopic(DAILY)
        if (language == GREEK_LANGUAGE) {
            FirebaseMessaging.getInstance().subscribeToTopic(DAILY_EL)
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic(DAILY_EN)
        }
    }

    fun unregisterDailyNotifications() {
        preferences.dailyNotifications = false
        val language = Locale.getDefault().language
        FirebaseMessaging.getInstance().unsubscribeFromTopic(DAILY)
        if (language == GREEK_LANGUAGE) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(DAILY_EL)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(DAILY_EN)
        }
    }

    fun registerPersonalNotifications() {
        preferences.personalNotifications = true
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            val language = Locale.getDefault().language
            registerToken(it.token, language)
        }
    }

    fun unregisterPersonalNotifications() {
        preferences.personalNotifications = false
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            val language = Locale.getDefault().language
            unregisterToken(it.token, language)
        }
    }

    private fun registerToken(token: String, language: String) {
        disposables.add(userService.registerFirebaseToken(token, language, HomeViewModel.OS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { })
    }

    private fun unregisterToken(token: String, language: String) {
        disposables.add(userService.unregisterFirebaseToken(token, language, HomeViewModel.OS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { })
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
                .doOnSubscribe {
                    loadingEmail.value = true
                    loadingPassword.value = true
                }
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
