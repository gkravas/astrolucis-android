package com.astrolucis.features.dailyPrediction

import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Menu
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.ActivityDailyPredictionBinding
import com.astrolucis.utils.dialogs.AlertDialog
import org.koin.android.architecture.ext.viewModel
import java.text.SimpleDateFormat
import java.util.*
import android.view.MenuItem
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolucis.GetDailyPredictionQuery
import com.astrolucis.utils.ErrorPresentation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class DailyPredictionActivity : BaseActivity() {

    companion object {
        const val NATAL_DATE_ID = "natalDateId"
        const val DATE = "date"
    }

    lateinit var binding: ActivityDailyPredictionBinding

    val viewModel: DailyPredictionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_daily_prediction)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        setSupportActionBar(binding.toolbar?.toolbar)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        binding.recyclerView.adapter = DailyPredictionExplanationsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = DefaultItemAnimator()

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        viewModel.messagesLiveData.observe(this, Observer {
            it?.let {
                when(it.type) {
                    ErrorPresentation.Companion.Type.DIALOG -> {
                        showAlertDialog(it.dialogId, AlertDialog.Data(viewModel::class), it.titleResId, it.messageResId)
                    }
                    ErrorPresentation.Companion.Type.SNACK_BAR -> {
                        showSnackBar(binding.root, it.titleResId)
                    }
                }

            }
        })

        viewModel.actionsLiveData.observe(this, Observer {
            if (it?.first == DailyPredictionViewModel.Action.RATE_DAILY_PREDICTION) {
                runOnUiThread({
                    var rating = (it.second as GetDailyPredictionQuery.DailyPrediction).accuracy()
                    rating = rating ?: 0
                    RatingAccuracyDialog.newInstance(rating.div(DailyPredictionViewModel.ACCURACY_MULTIPLIER).toInt(),
                            resources.getString(R.string.dailyPrediction_ratingDialog_title),
                            resources.getString(android.R.string.ok),
                            resources.getString(android.R.string.cancel))
                            .show(supportFragmentManager, RatingAccuracyDialog.RATING_DIALOG_ID)
                })
            }
        })

        viewModel.dailyPredictionLiveData.observe(this, Observer {
            it?.planetExplanations()?.let {
                (binding.recyclerView.adapter as DailyPredictionExplanationsAdapter).items = it
            }
        })

        intent.extras?.let {
            val locale = Locale("el")
            val calendar = Calendar.getInstance(locale)
            calendar.time = it[DATE] as Date
            supportActionBar?.title = resources.getString(R.string.dailyPrediction_title_text)

            supportActionBar?.subtitle = resources
                    .getString(R.string.dailyPrediction_subtitle_template,
                            SimpleDateFormat("EEEE", locale).format(it[DATE]),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            SimpleDateFormat("MMMM", locale).format(it[DATE]),
                            calendar.get(Calendar.YEAR))

            viewModel.loadPrediction(it[NATAL_DATE_ID] as Long,
                    SimpleDateFormat("yyyy-MM-dd", locale).format(it[DATE]))
        }

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.daily_prediction_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.rating_menu_item -> {
                viewModel.openRateDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
