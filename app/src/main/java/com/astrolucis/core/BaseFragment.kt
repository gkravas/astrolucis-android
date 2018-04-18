package com.astrolucis.core

import android.support.annotation.StringRes
import android.support.v4.app.Fragment

open class BaseFragment: Fragment() {
    val baseActivity: BaseActivity get() = activity as BaseActivity

    fun setActionBarTitle(@StringRes titleStringRes: Int) {
        baseActivity.supportActionBar?.let {
            it.setTitle(titleStringRes)
        }
    }
}