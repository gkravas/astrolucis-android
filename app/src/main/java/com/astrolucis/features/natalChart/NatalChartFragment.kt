package com.astrolucis.features.natalChart

import androidx.lifecycle.Observer
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import android.os.Bundle

import android.transition.TransitionManager
import android.view.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolucis.R
import com.astrolucis.core.BaseFragment
import com.astrolucis.databinding.FragmentNatalChartBinding
import com.astrolucis.features.natalDate.NatalDateFragment
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject


class NatalChartFragment : BaseFragment() {

    lateinit var binding: FragmentNatalChartBinding
    val viewModel: NatalChartViewModel by viewModel()

    override fun onStart() {
        super.onStart()
        viewModel.chartLiveData.observe(this, Observer {
            it?.let {
                binding.recyclerView.adapter = NatalChartAdapter(it)
            }
        })

        viewModel.actionsLiveData.observe(this, Observer {
            it?.let {
                when(it) {
                    NatalChartViewModel.Action.EDIT -> {
                        baseActivity.pushFragment(NatalDateFragment())
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setActionBarTitle(R.string.drawer_menu_natalDate)
        setHasOptionsMenu(true)
        binding = FragmentNatalChartBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.setLifecycleOwner(this)

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = DefaultItemAnimator()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.natal_chart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId ?: android.R.id.empty) {
            R.id.edit_menu_item -> {
                viewModel.editNatalDate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
