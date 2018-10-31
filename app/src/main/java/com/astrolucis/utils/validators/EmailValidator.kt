package com.astrolucis.utils.validators

import androidx.databinding.ObservableField
import android.util.Patterns

class EmailValidator(inputBindable: ObservableField<CharSequence>,
                     errorTextBindable: ObservableField<CharSequence>,
                     errorText: CharSequence) : ObservableValidator(inputBindable, errorTextBindable, errorText) {

    override fun onValidation(inputBindable: ObservableField<CharSequence>, errorTextBindable: ObservableField<CharSequence>): Boolean {
        return !inputBindable.get()?.isEmpty()!! && Patterns.EMAIL_ADDRESS.matcher(inputBindable.get()).matches()
    }
}