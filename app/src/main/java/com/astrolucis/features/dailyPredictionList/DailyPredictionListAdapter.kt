package com.astrolucis.features.dailyPredictionList

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.CardPredictionBinding
import com.astrolucis.features.home.HomeActivity
import com.squareup.picasso.Picasso
import org.koin.android.architecture.ext.KoinFactory
import java.text.SimpleDateFormat
import java.util.*

class DailyPredictionListAdapter: RecyclerView.Adapter<DailyPredictionListAdapter.ViewHolder>() {

    companion object {
         const val WEEK_DAYS: Int = 7
    }

    var viewModel: DailyPredictionListViewModel? = null

    var items: List<Date> = arrayListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        CardPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(CardPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val context = holder.itemView.context

        holder.binding?.titleTextView?.let {
            val locale = Locale("el")
            val calendar = Calendar.getInstance(locale)
            calendar.time = item
            it.text = context.resources
                    .getString(R.string.dailyPredictionList_cardTitle_template,
                            SimpleDateFormat("EEEE", locale).format(item),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            SimpleDateFormat("MMMM", locale).format(item),
                            calendar.get(Calendar.YEAR))
        }

        holder.binding?.imageView?.let {
            Picasso.with(context)
                    .load(context.resources.getString(R.string.dailyPredictionList_imageUrl_template,
                            (position % WEEK_DAYS) + 1))
                    .into(it)

            it.setOnClickListener {
                viewModel?.predictionSelected(items[position])
            }
        }
        holder.binding?.openButton?.let {
            it.setOnClickListener {
                viewModel?.predictionSelected(items[position])
            }
        }
    }

    class ViewHolder(val binding: CardPredictionBinding?) : RecyclerView.ViewHolder(binding?.root)
}