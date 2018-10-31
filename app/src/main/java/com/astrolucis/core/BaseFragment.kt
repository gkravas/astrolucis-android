package com.astrolucis.core

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

import com.astrolucis.utils.dialogs.AlertDialog

open class BaseFragment: Fragment() {
    val baseActivity: BaseActivity get() = activity as BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    fun setActionBarTitle(@StringRes titleStringRes: Int) {
        baseActivity.supportActionBar?.let {
            it.setTitle(titleStringRes)
        }
    }

    fun showAlertDialog(baseFragment: BaseFragment, id: String, data: AlertDialog.Data<*>, @StringRes title: Int, @StringRes message: Int) {
        showAlertDialog(baseFragment, id, data, resources.getText(title), resources.getText(message))
    }

    fun showAlertDialog(baseFragment: BaseFragment, id: String, data: AlertDialog.Data<*>, title: CharSequence, message: CharSequence) {
        activity?.runOnUiThread {
            AlertDialog.newInstance(id, data, title, message,
                    resources.getString(android.R.string.ok),
                    resources.getString(android.R.string.cancel)).apply {
                        this.setTargetFragment(baseFragment, 0)
                    }
                    .show(fragmentManager, id)
        }
    }
}