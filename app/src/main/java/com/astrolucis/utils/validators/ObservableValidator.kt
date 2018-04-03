package com.astrolucis.utils.validators

import android.databinding.ObservableField

open class ObservableValidator(private val inputBindable: ObservableField<CharSequence>,
                          private val errorTextBindable: ObservableField<CharSequence>,
                          private val errorText: CharSequence) {

    val isValid: Boolean
        get() {
            val result = onValidation(inputBindable, errorTextBindable)
            errorTextBindable.set(if (result) "" else errorText)
            return result
        }

    open fun onValidation(inputBindable: ObservableField<CharSequence>,
                               errorTextBindable: ObservableField<CharSequence>): Boolean {
        return false
    }
}