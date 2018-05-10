package com.astrolucis.features.dailyPrediction

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.astrolucis.GetDailyPredictionQuery
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.CardExplanationBinding
import com.astrolucis.databinding.CardPredictionBinding
import com.squareup.picasso.Picasso
import org.koin.android.architecture.ext.KoinFactory
import java.text.SimpleDateFormat
import java.util.*

class DailyPredictionExplanationsAdapter: RecyclerView.Adapter<DailyPredictionExplanationsAdapter.ViewHolder>() {

    companion object {
         const val WEEK_DAYS: Int = 7
    }

    var items: List<GetDailyPredictionQuery.PlanetExplanation> = arrayListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        CardExplanationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(CardExplanationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding?.titleTextView?.let {
            it.text = item.title()
        }

        holder.binding?.bodyTextView?.let {
            it.text = item.lemma()
        }
    }

    class ViewHolder(val binding: CardExplanationBinding?) : RecyclerView.ViewHolder(binding?.root)
}