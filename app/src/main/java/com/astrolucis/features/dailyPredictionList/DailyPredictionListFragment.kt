package com.astrolucis.features.dailyPredictionList

import android.content.Context
import android.content.Intent
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseFragment
import com.astrolucis.databinding.FragmentDailyPredictionsBinding
import com.astrolucis.features.dailyPrediction.DailyPredictionActivity
import org.koin.android.architecture.ext.viewModel
import java.util.*

class DailyPredictionListFragment : BaseFragment() {

    lateinit var binding: FragmentDailyPredictionsBinding
    val viewModel: DailyPredictionListViewModel by viewModel()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setActionBarTitle(R.string.drawer_menu_dailyPredictions)

        viewModel.listLiveData.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                (binding.recyclerView.adapter as DailyPredictionListAdapter).items = it
            }
        })
        viewModel.actionsLiveData.observe(this, android.arch.lifecycle.Observer {
            when(it?.first) {
                DailyPredictionListViewModel.Action.GO_TO_DAILY_PREDICTION -> {
                    Intent(context, DailyPredictionActivity::class.java).apply {
                        this.putExtra(DailyPredictionActivity.NATAL_DATE_ID, 1.toLong())
                        this.putExtra(DailyPredictionActivity.DATE, it.second as Date)
                        startActivity(this)
                    }
                }
            }
        })
        viewModel.initForm()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDailyPredictionsBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        binding.recyclerView.adapter = DailyPredictionListAdapter()
        (binding.recyclerView.adapter as DailyPredictionListAdapter).viewModel = viewModel
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = DefaultItemAnimator()

        return binding.root
    }
}