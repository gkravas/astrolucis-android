package com.astrolucis.features.home

import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.ViewGroup
import com.astrolucis.R
import com.astrolucis.core.BaseActivity
import com.astrolucis.databinding.ActivityHomeBinding
import com.astrolucis.features.dailyPredictionList.DailyPredictionListFragment
import com.astrolucis.features.login.LoginActivity
import com.astrolucis.features.natalDate.NatalDateFragment
import com.astrolucis.features.profile.ProfileFragment
import com.astrolucis.utils.routing.AppRouter
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import android.widget.Toast




class HomeActivity: BaseActivity() {


    companion object {
        const val OPEN_NATAL_DATE = "OPEN_NATAL_DATE"
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

        viewModel.viewState.observeForever( { viewState: HomeViewModel.ViewState? ->
            when (viewState) {
                HomeViewModel.ViewState.PROFILE -> {
                    pushFragment(ProfileFragment())
                    binding.navigation.post({ binding.navigation.setCheckedItem(R.id.profile_menu_item) })
                }
                HomeViewModel.ViewState.NATAL_DATE -> {
                    pushFragment(NatalDateFragment())
                    binding.navigation.post({ binding.navigation.setCheckedItem(R.id.natal_date_menu_item) })
                }
                HomeViewModel.ViewState.DAILY_PREDICTION_LIST -> {
                    pushFragment(DailyPredictionListFragment())
                    binding.navigation.post({ binding.navigation.setCheckedItem(R.id.daily_prediction_menu_item) })
                }
                HomeViewModel.ViewState.LOGOUT -> {
                    appRouter.goTo(LoginActivity::class, this)
                }
                HomeViewModel.ViewState.STAY_THERE -> {

                }
            }
        })

        binding.navigation.setNavigationItemSelectedListener({
            when (it.itemId) {
                R.id.logout_menu_item -> viewModel.logout()
                R.id.profile_menu_item -> viewModel.goToProfile()
                R.id.natal_date_menu_item -> viewModel.goToNatalDate()
                R.id.daily_prediction_menu_item -> viewModel.goToDailyPrediction()
            }
            binding.drawerLayout.closeDrawers()
            true
        })
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
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
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
}