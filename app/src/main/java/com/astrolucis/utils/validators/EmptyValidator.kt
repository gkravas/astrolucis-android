package com.astrolucis.utils.validators

import android.databinding.ObservableField
import android.util.Patterns

class EmptyValidator(inputBindable: ObservableField<CharSequence>,
                     errorTextBindable: ObservableField<CharSequence>,
                     errorText: CharSequence) : ObservableValidator(inputBindable, errorTextBindable, errorText) {

    override fun onValidation(inputBindable: ObservableField<CharSequence>, errorTextBindable: ObservableField<CharSequence>): Boolean {
        return !inputBindable.get()?.isEmpty()!!
    }
}