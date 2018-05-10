package com.astrolucis.features.dailyPrediction

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import com.astrolucis.R
import com.astrolucis.databinding.DialogRatingBinding
import org.koin.android.architecture.ext.KoinFactory

class RatingAccuracyDialog: AppCompatDialogFragment() {

    companion object {
        public const val RATING_DIALOG_ID = "ratingDialogId"
        private const val TAG = "AlertDialog"
        private const val RATING = "$TAG.rating"
        private const val TITLE = "$TAG.title"
        private const val OK = "$TAG.ok"
        private const val CANCEL = "$TAG.cancel"

        fun newInstance(rating: Int, title: CharSequence = "", ok: CharSequence = "",
                        cancel: CharSequence = ""): RatingAccuracyDialog {

            return RatingAccuracyDialog().also {
                it.arguments = Bundle().apply {
                    this.putInt(RATING, rating)
                    this.putCharSequence(TITLE, title)
                    this.putCharSequence(OK, ok)
                    this.putCharSequence(CANCEL, cancel)
                }
            }
        }
    }

    lateinit var viewModel: DailyPredictionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), KoinFactory)[DailyPredictionViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogRatingBinding = DataBindingUtil
                .inflate(LayoutInflater.from(context), R.layout.dialog_rating, null, false)

        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
        arguments?.let {

            builder.setView(binding.root)

            binding.ratingBar.progress = it[RATING] as Int

            val titleLemma = it[TITLE] as String
            if (titleLemma.isNotEmpty()) {
                builder.setTitle(titleLemma)
            }

            val okLemma = it[OK] as String
            if (okLemma.isNotEmpty()) {
                builder.setPositiveButton(okLemma) { _, _ ->
                    viewModel.onAccuracySubmission(binding.ratingBar.progress.toLong())
                }
            }
            val cancelLemma = it[CANCEL] as String
            if (cancelLemma.isNotEmpty()) {
                builder.setNegativeButton(cancelLemma) { _, _ ->
                    this.targetFragment?.activity?.runOnUiThread({
                        dismiss()
                    })
                }
            }
        }
        return builder.create()
    }
}