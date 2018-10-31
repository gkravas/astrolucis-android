package com.astrolucis.utils.validators

import androidx.databinding.ObservableField

class EqualFieldsValidator : ObservableValidator {

    val equalBindable: ObservableField<CharSequence>

    constructor(inputBindable: ObservableField<CharSequence>,
                equalBindable: ObservableField<CharSequence>,
                errorTextBindable: ObservableField<CharSequence>,
                errorText: CharSequence) : super(inputBindable, errorTextBindable, errorText) {

        this.equalBindable = equalBindable
    }

    override fun onValidation(inputBindable: ObservableField<CharSequence>, errorTextBindable: ObservableField<CharSequence>): Boolean {
        return equalBindable.get() == inputBindable.get()
    }
}