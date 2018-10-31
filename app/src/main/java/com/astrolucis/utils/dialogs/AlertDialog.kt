package com.astrolucis.utils.dialogs

import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import com.astrolucis.core.BaseViewModel
import org.koin.android.architecture.ext.KoinFactory
import java.io.Serializable
import java.lang.RuntimeException
import kotlin.reflect.KClass

class AlertDialog: AppCompatDialogFragment() {

    data class Data<T: BaseViewModel>(val viewModelClass: KClass<T>) : Serializable

    companion object {
        public const val LOGOUT_DIALOG_ID = "logoutDialogId"
        private const val TAG = "AlertDialog"
        private const val ID = "$TAG.id"
        private const val TITLE = "$TAG.title"
        private const val MESSAGE = "$TAG.message"
        private const val OK = "$TAG.ok"
        private const val CANCEL = "$TAG.cancel"
        private const val DATA = "$TAG.data"

        fun newInstance(id: String = "", data: Data<*>?, title: CharSequence = "", message: CharSequence = "",
                        ok: CharSequence = "",
                        cancel: CharSequence = ""): AlertDialog {

            return AlertDialog().also {
                it.arguments = Bundle().apply {
                    this.putCharSequence(ID, id)
                    this.putCharSequence(TITLE, title)
                    this.putCharSequence(MESSAGE, message)
                    this.putCharSequence(OK, ok)
                    this.putCharSequence(CANCEL, cancel)
                    this.putSerializable(DATA, data)
                }
            }
        }
    }

    lateinit var viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = arguments!![DATA]
        if (data is Data<*>) {
            viewModel = try {
                targetFragment?.let {
                    ViewModelProviders.of(it, KoinFactory)[data.viewModelClass.java]
                } ?: ViewModelProviders.of(requireActivity(), KoinFactory)[data.viewModelClass.java]
            } catch (e: RuntimeException) {
                ViewModelProviders.of(requireActivity(), KoinFactory)[data.viewModelClass.java]
            }
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        arguments?.let {
            val id = it[ID] as String

            builder.setMessage(it[MESSAGE] as String)

            val titleLemma = it[TITLE] as String
            if (titleLemma.isNotEmpty()) {
                builder.setTitle(titleLemma)
            }

            val okLemma = it[OK] as String
            if (okLemma.isNotEmpty()) {
                builder.setPositiveButton(okLemma) { _, _ ->
                    val activity: FragmentActivity = this.targetFragment?.activity?.let { it } ?: this.requireActivity()
                    activity.runOnUiThread({
                        viewModel.onDialogAction(id, true)
                    })
                }
            }
            val cancelLemma = it[CANCEL] as String
            if (cancelLemma.isNotEmpty()) {
                builder.setNegativeButton(cancelLemma) { _, _ ->
                    val activity: FragmentActivity = this.targetFragment?.activity?.let { it } ?: this.requireActivity()
                    activity?.runOnUiThread({
                        viewModel.onDialogAction(id, false)
                    })
                }
            }
        }
        return builder.create()
    }
}