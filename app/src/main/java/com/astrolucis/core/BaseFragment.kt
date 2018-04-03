package com.astrolucis.core

import android.support.v4.app.Fragment

open class BaseFragment: Fragment() {
    val baseActivity: BaseActivity get() = activity as BaseActivity
}