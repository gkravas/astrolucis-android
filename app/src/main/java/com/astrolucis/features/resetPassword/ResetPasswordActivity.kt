package com.astrolucis.features.resetPassword

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.ActivityResetPasswordBinding
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.utils.dialogs.AlertDialog
import com.astrolucis.utils.routing.AppRouter
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

class ResetPasswordActivity: BaseActivity() {

    companion object {
        const val TOKEN: String = "t"
    }
    lateinit var binding: ActivityResetPasswordBinding

    val viewModel: ResetPasswordViewModel by viewModel()
    val appRouter: AppRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        viewModel.messagesLiveData.observe(this, Observer {
            it?.let {
                showAlertDialog(it.dialogId,
                        AlertDialog.Data(ResetPasswordViewModel::class),
                        it.titleResId, it.messageResId)
            }
        })

        viewModel.actionsLiveData.observe(this, Observer {
            when(it) {
                ResetPasswordViewModel.Action.GO_TO_HOME -> appRouter.goTo(HomeActivity::class, this)
            }
        })

        viewModel.loading.observe(this, Observer {
            it?.let {
                binding.progressBar.visibility = if (it)  View.VISIBLE else View.GONE
                binding.saveButton.isEnabled = !it
            }
        })
        viewModel.initForm()
    }

    override fun onDestroy() {
        viewModel.reset()
        super.onDestroy()
    }
}