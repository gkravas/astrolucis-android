package com.astrolucis.utils.validators

import androidx.databinding.ObservableField

class RangeValidator(inputBindable: ObservableField<CharSequence>,
                     errorTextBindable: ObservableField<CharSequence>,
                     errorText: CharSequence, private val from: Int,
                     private val to: Int) : ObservableValidator(inputBindable, errorTextBindable, errorText) {

    override fun onValidation(inputBindable: ObservableField<CharSequence>,
                               errorTextBindable: ObservableField<CharSequence>): Boolean {
        return inputBindable.get()?.length in from..to
    }
}
