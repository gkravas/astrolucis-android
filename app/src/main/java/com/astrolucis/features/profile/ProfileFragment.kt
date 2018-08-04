package com.astrolucis.features.profile

import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseFragment
import com.astrolucis.databinding.FragmentProfileBinding
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.utils.dialogs.AlertDialog
import com.astrolucis.utils.routing.AppRouter
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

class ProfileFragment: BaseFragment() {

    lateinit var binding: FragmentProfileBinding

    val viewModel: ProfileViewModel by viewModel()
    val appRouter: AppRouter by inject()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        setActionBarTitle(R.string.drawer_menu_profile)

        viewModel.messagesLiveData.observe(this, Observer {
            it?.let {
                baseActivity.showAlertDialog(it.dialogId, AlertDialog.Data(viewModel::class), it.titleResId, it.messageResId)
            }
        })

        viewModel.actionsLiveData.observe(this, Observer {
            when(it) {
                ProfileViewModel.Action.GO_TO_HOME -> appRouter.goTo(HomeActivity::class, baseActivity)
            }
        })

        viewModel.loadingEmail.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.emailTextView.isEnabled = !it
                binding.progressBarEmail.visibility = if (it)  View.VISIBLE else View.GONE
                binding.saveButtonEmail.isEnabled = !it
            }
        })

        viewModel.loadingPassword.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.passwordTextView.isEnabled = !it
                binding.passwordRepeatTextView.isEnabled = !it
                binding.progressBarPassword.visibility = if (it)  View.VISIBLE else View.GONE
                binding.saveButtonPassword.isEnabled = !it
            }
        })

        viewModel.dailyNotifications.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.switchDailyNotifications.isChecked = it
                if (it) {
                    viewModel.registerDailyNotifications()
                } else {
                    viewModel.unregisterDailyNotifications()
                }
            }
        })

        viewModel.personalNotifications.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                binding.switchPersonalNotifications.isChecked = it
                if (it) {
                    viewModel.registerPersonalNotifications()
                } else {
                    viewModel.unregisterPersonalNotifications()
                }
            }
        })

        viewModel.initForm()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        return binding.root
    }
}