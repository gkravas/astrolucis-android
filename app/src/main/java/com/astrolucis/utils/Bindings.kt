package com.astrolucis.utils

import androidx.databinding.BindingAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.LayoutRes
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import android.widget.EditText
import com.astrolucis.core.ExtendedTextInputLayout


@BindingAdapter("visibility")
fun bindViewVisibility(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.INVISIBLE
}


@BindingAdapter("visibilityGone")
fun bindViewVisibilityGone(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.GONE
}

@BindingAdapter("entries")
fun bindAutoCompleteEntries(view: AutoCompleteTextView, @ArrayRes entries: Int) {
    @LayoutRes val layoutItemId: Int = android.R.layout.simple_dropdown_item_1line
    val entryArr: Array<String> = view.resources.getStringArray(entries)
    view.setAdapter(ArrayAdapter(view.context, layoutItemId, entryArr))
}

@BindingAdapter("onFocusChange")
fun onFocusChange(text: EditText, listener: View.OnFocusChangeListener) {
    text.onFocusChangeListener = listener
}

@BindingAdapter("helperText")
fun helperText(text: ExtendedTextInputLayout, stringRes: Int) {
    text.helperText = text.context.getText(stringRes)
}
/*
@BindingAdapter("threshold")
fun bindAutoCompleteThreshold(view: AutoCompleteTextView, threshold: Int) {
    view.threshold = threshold
    if (threshold == 0) {
        view.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                view.showDropDown()
            }
        }
    }
}*/
