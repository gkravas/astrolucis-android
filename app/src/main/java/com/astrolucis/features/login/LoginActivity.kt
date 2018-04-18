package com.astrolucis.features.login

import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.transition.TransitionManager
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.ActivityLoginBinding
import com.astrolucis.features.home.HomeActivity
import com.astrolucis.utils.dialogs.AlertDialog
import com.astrolucis.utils.routing.AppRouter
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import java.util.*


class LoginActivity : BaseActivity() {

    companion object {
        const val FB_EMAIL_PERMISSION: String = "email"
        const val FB_PUBLIC_PROFILE_PERMISSION: String = "public_profile"
    }

    lateinit var binding: ActivityLoginBinding
    lateinit var callbackManager: CallbackManager


    val viewModel: LoginViewModel by viewModel()
    val appRouter: AppRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

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
                showAlertDialog(it.dialogId, AlertDialog.Data(viewModel::class), it.titleResId, it.messageResId)
            }
        })

        viewModel.actionsLiveData.observe(this, Observer {
            when(it) {
                LoginViewModel.Action.GO_TO_HOME -> appRouter.goTo(HomeActivity::class, this)
            }
        })

        callbackManager = CallbackManager.Factory.create()

        binding.fbLoginButton.setReadPermissions(Arrays.asList(FB_EMAIL_PERMISSION, FB_PUBLIC_PROFILE_PERMISSION))

        binding.fbLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                viewModel.fbLogin(loginResult.accessToken.token)
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })

        viewModel.fbLogin(AccessToken.getCurrentAccessToken()?.token)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
