package com.astrolucis.features.dailyPrediction

import androidx.lifecycle.ViewModelProviders
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.astrolucis.GetDailyPredictionQuery
import com.astrolucis.databinding.CardExplanationBinding
import org.koin.android.architecture.ext.KoinFactory

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

    class ViewHolder(val binding: CardExplanationBinding?) : RecyclerView.ViewHolder(binding?.root!!)
}