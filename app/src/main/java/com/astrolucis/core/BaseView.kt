package com.astrolucis.core

import androidx.annotation.StringRes

interface BaseView {
    fun showLoadingIndicator()
    fun hideLoadingIndicator()
    fun openDialog(@StringRes titleResId: Int, @StringRes bodyResId: Int)
    fun showToast(@StringRes messageId: Int)
}
