package com.astrolucis.utils.validators

import androidx.databinding.ObservableField

class EmptyValidator(inputBindable: ObservableField<CharSequence>,
                     errorTextBindable: ObservableField<CharSequence>,
                     errorText: CharSequence) : ObservableValidator(inputBindable, errorTextBindable, errorText) {

    override fun onValidation(inputBindable: ObservableField<CharSequence>, errorTextBindable: ObservableField<CharSequence>): Boolean {
        return !inputBindable.get()?.isEmpty()!!
    }
}