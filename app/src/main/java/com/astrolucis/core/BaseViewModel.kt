package com.astrolucis.core

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    open fun onDialogAction(id: String, positive: Boolean) {

    }
}