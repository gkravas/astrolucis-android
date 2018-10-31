package com.astrolucis.features.natalChart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.astrolucis.databinding.CardPredictionBinding
import com.astrolucis.databinding.RowNatalChartHeaderBinding
import com.astrolucis.databinding.RowNatalChartRowBinding
import com.astrolucis.models.natalDate.Chart

class NatalChartAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        val aspects: List<Pair<Int, String>> = arrayListOf(
                Pair(0, "conjunct"),
                Pair(30, "semisextile"),
                Pair(45, "semisquare"),
                Pair(60, "sextile"),
                Pair(72, "quintile"),
                Pair(90, "square"),
                Pair(120, "trine"),
                Pair(135, "sesquiquadrate"),
                Pair(150, "inconjunct"),
                Pair(180, "opposition")
        )

        const val TYPE_HEADER = 0
        const val TYPE_ROW = 1
    }

    private val items: List<Pair<Boolean, String>>

    constructor(chart: Chart) : super() {
        items = generateItems(chart)
    }

    private fun generateItems(chart: Chart): List<Pair<Boolean, String>> {
        val result: MutableList<Pair<Boolean, String>> = arrayListOf()

        result.add(Pair(true, "Planets"))
        chart.planets?.forEach {
            it?.let {
                val retrogate: String = if (it.retrogate!!) "Retrogate " else ""
                result.add(Pair(false, "$retrogate${it.name} on ${it.sign}, on ${it.house!!}${getOrdinal(it.house!!)} house at ${it.angle!!.toInt()}˚"))
            }
        }

        result.add(Pair(true, "Aspects"))
        chart.aspects?.forEach {
            it?.let {
                val aspect = it
                result.add(Pair(false, "${aspect.planet1} in ${aspects.firstOrNull { it.first == aspect.angle}} ${aspect.planet2}"))
            }
        }
        return result
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].first) TYPE_HEADER else TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        CardPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(RowNatalChartHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            RowViewHolder(RowNatalChartRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is HeaderViewHolder) {
            holder.binding?.text?.text = item.second
        } else if (holder is RowViewHolder) {
            holder.binding?.text?.text = item.second
        }
    }

    private fun getOrdinal(n: Int): String {
        if (n in 11..13) {
            return "th"
        }
        return when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    class HeaderViewHolder(val binding: RowNatalChartHeaderBinding?) : RecyclerView.ViewHolder(binding?.root!!)
    class RowViewHolder(val binding: RowNatalChartRowBinding?) : RecyclerView.ViewHolder(binding?.root!!)
}