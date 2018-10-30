package com.astrolucis.features.natalDate

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.databinding.Observable
import android.databinding.ObservableField
import android.view.View
import com.astrolucis.R
import com.astrolucis.core.BaseViewModel
import com.astrolucis.di.App
import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.models.NatalType
import com.astrolucis.models.natalDate.Chart
import com.astrolucis.services.interfaces.NatalDateService
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService
import com.astrolucis.utils.ErrorHandler
import com.astrolucis.utils.ErrorPresentation
import com.astrolucis.utils.dialogs.AlertDialog.Companion.LOGOUT_DIALOG_ID
import com.astrolucis.utils.validators.EmptyValidator
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class NatalDateViewModel : BaseViewModel {

    companion object {
        const val DEFAULT_NAME: String = "me"
        const val SAVE_COMPLETE_DIALOG_ID: String = "saveCompleteDialogId"
    }

    enum class Action {
        OPEN_DATE_PICKER,
        OPEN_TIME_PICKER,
        OPEN_TYPE_PICKER,
        SAVE_COMPLETE,
        GO_TO_HOME
    }

    val userService: UserService
    val natalDateService: NatalDateService
    val preferences: Preferences

    val idField: ObservableField<CharSequence>
    val nameField: ObservableField<CharSequence>
    val livingLocationField: ObservableField<CharSequence>
    val birthLocationField: ObservableField<CharSequence>
    val birthDateField: ObservableField<CharSequence>
    val birthTimeField: ObservableField<CharSequence>

    val livingLocationError: ObservableField<CharSequence>
    val birthLocationError: ObservableField<CharSequence>
    val birthDateError: ObservableField<CharSequence>
    val birthTimeError: ObservableField<CharSequence>
    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val typeField: MutableLiveData<Int> = MutableLiveData()
    val types: MutableLiveData<List<String>> = MutableLiveData()

    val disposables = CompositeDisposable()

    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()
    val messagesLiveData: MutableLiveData<ErrorPresentation> = MutableLiveData()

    constructor(application: Application, userService: UserService, natalDateService: NatalDateService, preferences: Preferences) : super(application) {

        this.userService = userService
        this.natalDateService = natalDateService
        this.preferences = preferences

        this.idField = ObservableField("")
        this.nameField = ObservableField("")
        this.livingLocationField = ObservableField("")
        this.birthLocationField = ObservableField("")
        this.birthDateField = ObservableField("")
        this.birthTimeField = ObservableField("")

        this.livingLocationError = ObservableField("")
        this.birthLocationError = ObservableField("")
        this.birthDateError = ObservableField("")
        this.birthTimeError = ObservableField("")

        this.loading.postValue(false)

        this.typeField.value = 0
        this.types.value = NatalType.values()
                .filter { it != NatalType.UNKNOWN }
                .map { application.resources.getString(it.resourceId) }

        this.livingLocationField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validateLivingLocation()
            }
        })

        this.birthLocationField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validateBirthLocation()
            }
        })

        this.birthDateField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validateBirthDate()
            }
        })

        this.birthTimeField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(field: Observable?, arg: Int) {
                validateBirthTime()
            }
        })

        loadNatalDate()
    }

    private fun loadNatalDate() {
        disposables.add(natalDateService.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loading.value = true }
                .subscribe(
                        { me ->
                            showNatalDate(me?.location(),
                                    me?.natalDates()
                                            ?.firstOrNull()
                                            ?.fragments()
                                            ?.natalDateFragment())
                            loading.value = false
                        },
                        { throwable ->
                            messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                            loading.value = false
                        }
                ))
    }

    private fun showNatalDate(livingLocation: String?, natalDate: NatalDateFragment?) {
        val context = getApplication<App>().baseContext

        val livingLocationFinal = if (livingLocation.isNullOrEmpty()) context.getText(R.string.natalDate_defaultCountry) else livingLocation
        idField.set(natalDate?.id().toString())
        nameField.set(natalDate?.name().orEmpty())
        birthLocationField.set(natalDate?.location().orEmpty())
        livingLocationField.set(livingLocationFinal)

        val parsedDate: Date? = natalDate?.date()?.let { parseServerDate(it, natalDate.timezoneMinutesDifference()) }

        val type: NatalType = NatalType.findBy(natalDate?.type().toString())
        if (parsedDate == null) {
            nameField.set(DEFAULT_NAME)
            birthTimeField.set(context.getText(R.string.natalDate_defaultTime))
        } else {
            val locale = Locale("el-GR")
            nameField.set(natalDate.name())
            birthDateField.set(SimpleDateFormat("dd/MM/yyyy", locale).format(parsedDate))
            birthTimeField.set(SimpleDateFormat("HH:mm", locale).format(parsedDate))
        }

        idField.notifyChange()
        nameField.notifyChange()
        livingLocationField.notifyChange()
        birthLocationField.notifyChange()
        birthDateField.notifyChange()
        birthTimeField.notifyChange()
        types.value?.indexOf(getApplication<App>().resources.getString(type.resourceId))?.let {
            typeField.postValue(it + 1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    private fun validateLivingLocation(): Boolean {
        return EmptyValidator(livingLocationField, livingLocationError,
                getApplication<App>().getText(R.string.natalDate_livingLocation_emptyError)).isValid
    }

    private fun validateBirthLocation(): Boolean {
        return EmptyValidator(birthLocationField, birthLocationError,
                getApplication<App>().getText(R.string.natalDate_birthLocation_emptyError)).isValid
    }

    private fun validateBirthDate(): Boolean {
        return EmptyValidator(birthDateField, birthDateError,
                getApplication<App>().getText(R.string.natalDate_birthDate_emptyError)).isValid
    }

    private fun validateBirthTime(): Boolean {
        return EmptyValidator(birthTimeField, birthTimeError,
                getApplication<App>().getText(R.string.natalDate_birthTime_emptyError)).isValid
    }

    fun onFocusChangeListener(): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                when(view.id) {
                    R.id.birth_date_text_view -> openDatePicker()
                    R.id.birth_time_text_view -> openTimePicker()
                    R.id.type_auto_complete -> openTypePicker()
                }
            }
        }
    }

    fun openDatePicker() {
        actionsLiveData.value = Action.OPEN_DATE_PICKER
    }

    fun openTimePicker() {
        actionsLiveData.value = Action.OPEN_TIME_PICKER
    }

    fun openTypePicker() {
        actionsLiveData.value = Action.OPEN_TYPE_PICKER
    }

    private fun parseServerDate(date: String, timezoneMinutesDifference: Long): Date? {
        return try {
            val time: Date = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss").parse(date)
            val cal: Calendar = Calendar.getInstance()
            cal.time = time
            cal.add(Calendar.SECOND, timezoneMinutesDifference.toInt())
            cal.time
        } catch (e: ParseException) {
            null
        }
    }

    private fun transformClientDateToServer(date: String): String? {
        return try {
            val clientDate = SimpleDateFormat("dd/MM/yyyy HH:mm").parse(date)
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(clientDate).replace(" ", "T")
        } catch (e: ParseException) {
            null
        }
    }

    fun save() {
        if (!validateLivingLocation() || !validateBirthLocation() ||
                !validateBirthDate() || !validateBirthTime()) {
            return
        }

        val id: Long? = try {
            idField.get().toString().toLong()
        } catch (e: NumberFormatException) {
            null
        }
        val date = transformClientDateToServer("${birthDateField.get()} ${birthTimeField.get()}") ?: ""
        val birthLocation = birthLocationField.get().toString()
        val name = nameField.get().toString()
        val type: String = NatalType.values()[typeField.value!!].value

        val createUpdateNatalDate = if (id == null) {
            natalDateService.createNatalDateMutation(date, birthLocation, name, true, type)
        } else {
            natalDateService.updateNatalDateMutation(id, date, birthLocation, name, true, type)
        }
        disposables.add(userService.updateLivingLocation(livingLocationField.get().toString())
                .flatMap { createUpdateNatalDate }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ loading.value = true })
                .subscribe(
                        {
                            messagesLiveData.value = ErrorPresentation(R.string.success_defaultTitle,
                                    R.string.natalDate_natalDateSaveComplete_text,
                                    SAVE_COMPLETE_DIALOG_ID)
                            showNatalDate(livingLocationField.get().toString(), it)
                            loading.value = false
                        },
                        { throwable ->
                            messagesLiveData.value = ErrorHandler.handleNatalDateError(throwable)
                            loading.value = false
                        }
                )
        )
    }

    override fun onDialogAction(id: String, positive: Boolean) {
        when (id) {
            LOGOUT_DIALOG_ID -> actionsLiveData.value = Action.GO_TO_HOME
            SAVE_COMPLETE_DIALOG_ID -> actionsLiveData.value = Action.SAVE_COMPLETE
        }
    }
}
