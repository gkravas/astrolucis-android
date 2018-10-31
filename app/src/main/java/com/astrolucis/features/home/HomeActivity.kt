package com.astrolucis.features.home

import androidx.lifecycle.Observer
import android.content.Intent
import android.content.res.Configuration
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.os.Handler
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.transition.TransitionManager
import android.view.ViewGroup
import android.widget.Toast
import com.astrolucis.BuildConfig
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.ActivityHomeBinding
import com.astrolucis.features.dailyPredictionList.DailyPredictionListFragment
import com.astrolucis.features.login.LoginActivity
import com.astrolucis.features.natalChart.NatalChartFragment
import com.astrolucis.features.natalDate.NatalDateFragment
import com.astrolucis.features.profile.ProfileFragment
import com.astrolucis.utils.dialogs.AlertDialog
import com.astrolucis.utils.routing.AppRouter
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject


class HomeActivity: BaseActivity() {


    companion object {
        const val OPEN_NATAL_DATE = "OPEN_NATAL_DATE"
        const val BLOG_URL = "https://www.gineastrologos.gr"
        const val GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}&referrer=utm_source%3DandroidApp"
    }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityHomeBinding
    private var doubleBackToExitPressedOnce: Boolean = false

    val viewModel : HomeViewModel by viewModel()
    val appRouter: AppRouter by inject()

    override fun getMasterContainerId(): Int {
        return R.id.content_frame
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setSupportActionBar(binding.toolbar?.toolbar)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        drawerToggle = setupDrawerToggle()
        binding.drawerLayout.addDrawerListener(drawerToggle)

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding?): Boolean {
                TransitionManager.beginDelayedTransition(binding!!.root as ViewGroup)
                return super.onPreBind(binding)
            }
        })

        viewModel.viewState.observe(this, Observer {
            when (it) {
                HomeViewModel.ViewState.PROFILE -> {
                    startWithFragment(ProfileFragment())
                    binding.navigation.post { binding.navigation.setCheckedItem(R.id.profile_menu_item) }
                }
                HomeViewModel.ViewState.NATAL_DATE -> {
                    startWithFragment(NatalDateFragment())
                    binding.navigation.post { binding.navigation.setCheckedItem(R.id.natal_date_menu_item) }
                }
                HomeViewModel.ViewState.NATAL_CHART-> {
                    startWithFragment(NatalChartFragment())
                    binding.navigation.post { binding.navigation.setCheckedItem(R.id.natal_date_menu_item) }
                }
                HomeViewModel.ViewState.DAILY_PREDICTION_LIST -> {
                    startWithFragment(DailyPredictionListFragment())
                    binding.navigation.post { binding.navigation.setCheckedItem(R.id.daily_prediction_menu_item) }
                }
                HomeViewModel.ViewState.LOGOUT -> {
                    appRouter.goTo(LoginActivity::class, this)
                }
                HomeViewModel.ViewState.SHOW_UPDATE_DIALOG -> {
                    showAlertDialog(HomeViewModel.UPDATE_DIALOG_ID, AlertDialog.Data(viewModel::class),
                            R.string.error_defaultTitle, R.string.updateDialog_message)
                }
                HomeViewModel.ViewState.GO_TO_GOOGLE_PLAY -> {
                    openURL(GOOGLE_PLAY)
                }
                HomeViewModel.ViewState.STAY_THERE -> {

                }
            }
        })

        binding.navigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout_menu_item -> viewModel.logout()
                R.id.profile_menu_item -> viewModel.goToProfile()
                R.id.natal_date_menu_item -> viewModel.goToNatalDate()
                R.id.daily_prediction_menu_item -> viewModel.goToDailyPrediction()
                R.id.blog_menu_item -> {
                    openURL(BLOG_URL)
                }
                R.id.share_item -> {
                    val share = Intent(android.content.Intent.ACTION_SEND)
                    share.type = "text/plain"
                    share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    share.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.motto))
                    share.putExtra(Intent.EXTRA_TEXT, GOOGLE_PLAY)
                    startActivity(Intent.createChooser(share, resources.getString(R.string.drawer_menu_share)))
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        viewModel.initFirebaseRemoteConfig()
        viewModel.initFirebaseMessaging()
    }

    override fun parseState(state: Bundle) {
        viewModel.viewState.value = if (state.getBoolean(OPEN_NATAL_DATE, false)) {
            HomeViewModel.ViewState.NATAL_DATE
        } else {
            HomeViewModel.ViewState.DAILY_PREDICTION_LIST
        }
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar?.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    }

    override fun onBackPressed() {
        if (!isAtRootFragment()) {
            popFragment()
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true)
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, R.string.back_again_to_exit, Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackStackChanged() {
        super.onBackStackChanged()
        if (!isAtRootFragment()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            drawerToggle.syncState()
        }
    }
}