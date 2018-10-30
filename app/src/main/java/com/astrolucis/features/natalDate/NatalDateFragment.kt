package com.astrolucis.features.natalDate

import android.content.Context
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.text.TextUtils
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.astrolucis.R
import com.astrolucis.core.BaseFragment
import com.astrolucis.databinding.FragmentNatalDateBinding
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.models.NatalType
import com.astrolucis.utils.dialogs.AlertDialog
import com.astrolucis.utils.routing.AppRouter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import java.util.*
import fr.ganfra.materialspinner.MaterialSpinner
import android.widget.ArrayAdapter




class NatalDateFragment : BaseFragment() {

    companion object {
        val DatePickerDialogTag: String = "DatePickerDialogTag"
        val TimePickerDialogTag: String = "TimePickerDialogTag"
    }

    lateinit var binding: FragmentNatalDateBinding
    val viewModel: NatalDateViewModel by viewModel()
    val appRouter: AppRouter by inject()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        setActionBarTitle(R.string.drawer_menu_natalDate)

        viewModel.actionsLiveData.observe(this, android.arch.lifecycle.Observer {
            when (it) {
                NatalDateViewModel.Action.OPEN_DATE_PICKER -> openDatePicker()
                NatalDateViewModel.Action.OPEN_TIME_PICKER -> openTimePicker()
                NatalDateViewModel.Action.OPEN_TYPE_PICKER -> openTypePicker()
                NatalDateViewModel.Action.SAVE_COMPLETE -> goToHome()
                NatalDateViewModel.Action.GO_TO_HOME -> appRouter.goTo(HomeActivity::class, baseActivity)
            }
        })
        viewModel.messagesLiveData.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                showAlertDialog(this@NatalDateFragment, it.dialogId, AlertDialog.Data(viewModel::class), it.titleResId, it.messageResId)
            }
        })

        viewModel.loading.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.progressBar.visibility = if (it)  View.VISIBLE else View.GONE
                binding.saveButton.isEnabled = !it
            }
        })

        viewModel.typeField.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.typeAutoComplete.setSelection(it)
            }
        })

        viewModel.types.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, it)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.typeAutoComplete.adapter = adapter
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNatalDateBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.setLifecycleOwner(this)

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        //binding.typeAutoComplete.onFocusChangeListener = viewModel.onFocusChangeListener()
        //make them non editable
        binding.birthDateTextView.keyListener = null
        binding.birthTimeTextView.keyListener = null

        return binding.root
    }

    private fun openDatePicker() {
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
                { _: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    binding.birthDateTextView.setText("${"%02d".format(dayOfMonth)}/${"%02d".format(monthOfYear + 1)}/${year} ")
                    if (!TextUtils.isEmpty(binding.birthTimeTextView.text)) {
                        openTimePicker()
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        dpd.setVersion(DatePickerDialog.Version.VERSION_2)
        dpd.show(activity?.fragmentManager, DatePickerDialogTag)
    }

    private fun openTimePicker() {
        val now = Calendar.getInstance()
        val dpd = TimePickerDialog.newInstance(
                { _: TimePickerDialog?, hourOfDay: Int, minute: Int, _: Int ->
                    binding.birthTimeTextView.setText("${"%02d".format(hourOfDay)}:${"%02d".format(minute)}")
                    if (!TextUtils.isEmpty(binding.typeAutoComplete.selectedItem as CharSequence?)) {
                        openTypePicker()
                    }
                }, now.get(Calendar.HOUR), now.get(Calendar.MINUTE), true)
        dpd.version = TimePickerDialog.Version.VERSION_2
        dpd.show(activity?.fragmentManager, TimePickerDialogTag)
    }

    private fun openTypePicker() {
        binding.typeAutoComplete.performClick()
    }

    private fun goToHome() {
        appRouter.goTo(HomeActivity::class, baseActivity)
    }
}
