package com.astrolucis.di

import android.app.Application
import com.astrolucis.BuildConfig
import com.astrolucis.features.dailyPredictionList.DailyPredictionListViewModel
import com.astrolucis.services.*
import com.astrolucis.features.home.HomeViewModel
import com.astrolucis.features.login.LoginViewModel
import com.astrolucis.features.natalDate.NatalDateViewModel
import com.astrolucis.features.profile.ProfileViewModel
import com.astrolucis.features.resetPassword.ResetPasswordViewModel
import com.astrolucis.utils.routing.AppRouter
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module
import java.net.CookieManager
import java.net.CookiePolicy

class App : Application() {


    private val appModule : Module = org.koin.dsl.module.applicationContext {
        bean { Preferences(instance) as com.astrolucis.services.interfaces.Preferences }
        bean { GraphQLService(get()) }
        bean { AppRouter(get(), get()) }
        bean { NatalDateService(get(), get()) as com.astrolucis.services.interfaces.NatalDateService}
        bean { UserService(get(), get(), get()) as com.astrolucis.services.interfaces.UserService}
        bean { RestService(get(), get()) }

        bean {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            cookieManager
        }

        viewModel { LoginViewModel(get(), get(), get()) }
        viewModel { HomeViewModel(get(), get()) }
        viewModel { NatalDateViewModel(get(), get(), get(), get()) }
        viewModel { ResetPasswordViewModel(get(), get(), get(), get()) }
        viewModel { ProfileViewModel(get(), get(), get()) }
        viewModel { DailyPredictionListViewModel(get()) }
    }

    companion object {
        lateinit var instance: App
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this, BuildConfig.ADD_MOB_ID)
        startKoin(this, listOf(appModule))
    }
}