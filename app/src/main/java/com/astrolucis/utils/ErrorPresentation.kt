package com.astrolucis.utils

data class ErrorPresentation(val titleResId: Int, val messageResId: Int,
                             val dialogId: String = DEFAULT_DIALOG_ID,
                             val type: Type = Type.DIALOG) {
    companion object {
        public const val DEFAULT_DIALOG_ID = "defaultDialogId"

        enum class Type {
            DIALOG,
            SNACK_BAR
        }
    }
}