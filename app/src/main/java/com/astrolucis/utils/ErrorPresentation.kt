package com.astrolucis.utils

data class ErrorPresentation(val titleResId: Int, val messageResId: Int,
                             val dialogId: String = DEFAULT_DIALOG_ID) {
    companion object {
        public const val DEFAULT_DIALOG_ID = "defaultDialogId"
    }
}